package io.truereactive.library.flow

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.reactivex.Observable
import io.truereactive.library.core.*
import io.truereactive.library.core.Optional
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import shark.ObjectInspector
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
class ReactiveApplicationCompat(app: Application) : ReactiveApplication,
    CoroutineScope {
    // CoroutineScope by MainScope() {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main.immediate

    val optionalReporter = ObjectInspector { reporter ->
        reporter.whenInstanceOf(Optional::class) { instance ->

            val label = instance[Optional::class, "logKey"]!!
            reporter.labels.add("${label.name}: ${label.value.readAsJavaString()}")
        }
    }

    // TODO: Use reduce like in fragment
    // TODO: possibly dispose when activity count is 0. Maybe sort of refCount base on activity count
    private val rxActivityCallbacks: SharedFlow<ActivityViewState<ViewEvents, Any>> by lazy(
        LazyThreadSafetyMode.NONE
    ) {
        callbackFlow<ActivityViewState<ViewEvents, Any>> {
            val callbacks = object : AbstractActivityCallbacks() {

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

                    activity.executeIfBase { baseActivity ->

                        bindPresenter(
                            baseActivity,
                            savedInstanceState,
                            rxActivityCallbacks,
                            baseActivity.intent.extras
                        )

                        offer(
                            ActivityViewState(
                                host = baseActivity,
                                view = null,
                                state = ViewState.Created,
                                key = baseActivity.viewIdKey,
                                restoredState = savedInstanceState
                            )
                        )
                    }
                }

                override fun onActivityStarted(activity: Activity) {

                    Timber.i("========== New state is active $isActive")
                    activity.executeIfBase { baseActivity ->
                        offer(
                            ActivityViewState(
                                host = baseActivity,
                                view = baseActivity.window.decorView.rootView,
                                state = ViewState.Started,
                                key = baseActivity.viewIdKey
                            )
                        )
                    }
                }

                override fun onActivityResumed(activity: Activity) {
                    activity.executeIfBase { baseActivity ->

                        offer(
                            ActivityViewState(
                                host = baseActivity,
                                view = baseActivity.window.decorView.rootView,
                                state = ViewState.Resumed,
                                key = baseActivity.viewIdKey
                            )
                        )
                    }
                }

                override fun onActivityPaused(activity: Activity) {
                    activity.executeIfBase { baseActivity ->
                        offer(
                            ActivityViewState(
                                host = baseActivity,
                                view = baseActivity.window.decorView.rootView,
                                state = ViewState.Paused,
                                key = baseActivity.viewIdKey
                            )
                        )
                    }
                }

                override fun onActivityStopped(activity: Activity) {
                    activity.executeIfBase { baseActivity ->
                        offer(
                            ActivityViewState(
                                host = baseActivity,
                                view = null,
                                state = ViewState.Stopped,
                                key = baseActivity.viewIdKey
                            )
                        )
                    }
                }

                override fun onActivityDestroyed(activity: Activity) {
                    Timber.d("Activity destroyed: ${activity::class.simpleName}, isFinishing ${activity.isFinishing} changing config ${activity.isChangingConfigurations}")
                    activity.executeIfBase { baseActivity ->

                        offer(
                            ActivityViewState(
                                host = baseActivity,
                                view = null,
                                state = ViewState.Destroyed,
                                key = baseActivity.viewIdKey
                            )
                        )

                        if (activity.isFinishing && !activity.isChangingConfigurations) {
                            offer(
                                ActivityViewState(
                                    host = baseActivity,
                                    view = null,
                                    state = ViewState.Dead,
                                    key = baseActivity.viewIdKey
                                )
                            )
                            die(baseActivity)
                        }
                    }
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                    activity.executeIfBase { baseActivity ->
                        outState.putString(ViewDelegate.VIEW_ID_KEY, baseActivity.viewIdKey)

                        offer(
                            ActivityViewState(
                                host = baseActivity,
                                view = null,
                                outState = outState,
                                state = ViewState.SavingState,
                                key = baseActivity.viewIdKey
                            )
                        )
                    }
                }

            }

            app.registerActivityLifecycleCallbacks(callbacks)

            awaitClose {
                app.unregisterActivityLifecycleCallbacks(callbacks)
            }
        }.flowOn(Dispatchers.Main.immediate)
            .onStart { Timber.i("========== Start activity callbacks") }
            .onCompletion { Timber.i("========== Stop activity callbacks") }
            .onEach { Timber.i("========== New state ${it.host::class.simpleName}, ${it.state.name}") }
            .shareIn(scope = this, started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0))
    }

    private val rxFragmentCallbacks: SharedFlow<FragmentViewState<ViewEvents, Any>> by lazy(
        LazyThreadSafetyMode.NONE
    ) {
        rxActivityCallbacks
            .filter { it.state == ViewState.Created }
            .flatMapLatest { activityState ->

                // This hack is need to properly handle fragment destruction in some cases (i.e. viewpager).
                //  In this case fragment is destroyed *after* the parent activity is destroyed,
                //  so it's not possible to simply end this stream, we need to handle all the fragments destruction an complete only after that.
                // TODO: extend this behavior for any fragment, because it's good to have an option to add 3rd party fragments which are not BaseFragment
                val refCount =
                    mutableMapOf<KClass<out BaseFragment<ViewEvents, Any>>, AtomicInteger>()

                callbackFlow<FragmentViewState<ViewEvents, Any>> {
                    val callbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
                        override fun onFragmentPreCreated(
                            fm: FragmentManager,
                            f: Fragment,
                            savedInstanceState: Bundle?
                        ) {

                            // TODO: Check if this method is called when rotated
                            f.executeIfBase { fragment ->
                                refCount.getOrPut(fragment::class, { AtomicInteger() })
                                    .incrementAndGet()

                                bindPresenter(
                                    fragment,
                                    savedInstanceState,
                                    rxFragmentCallbacks,
                                    fragment.arguments
                                )
                            }
                        }

                        override fun onFragmentViewCreated(
                            fm: FragmentManager,
                            f: Fragment,
                            v: View,
                            savedInstanceState: Bundle?
                        ) {
                            f.executeIfBase {
                                Timber.d("Fragment created: ${it::class.simpleName} — ${it.viewIdKey}")
                                offer(
                                    FragmentViewState(
                                        host = it,
                                        view = v,
                                        state = ViewState.Created,
                                        key = it.viewIdKey,
                                        restoredState = savedInstanceState
                                    )
                                )
                            }
                        }

                        override fun onFragmentSaveInstanceState(
                            fm: FragmentManager,
                            f: Fragment,
                            outState: Bundle
                        ) {
                            f.executeIfBase { fr ->
                                outState.putString(ViewDelegate.VIEW_ID_KEY, fr.viewIdKey)

                                offer(
                                    FragmentViewState(
                                        host = fr,
                                        view = null,
                                        outState = outState,
                                        state = ViewState.SavingState,
                                        key = fr.viewIdKey
                                    )
                                )
                            }
                        }

                        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
                            f.executeIfBase {
                                offer(
                                    FragmentViewState(
                                        host = it,
                                        view = null,
                                        state = ViewState.Started,
                                        key = it.viewIdKey
                                    )
                                )
                            }
                        }

                        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                            f.executeIfBase {
                                offer(
                                    FragmentViewState(
                                        host = it,
                                        view = null,
                                        state = ViewState.Resumed,
                                        key = it.viewIdKey
                                    )
                                )
                            }
                        }

                        override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
                            f.executeIfBase {
                                offer(
                                    FragmentViewState(
                                        host = it,
                                        view = null,
                                        state = ViewState.Paused,
                                        key = it.viewIdKey
                                    )
                                )
                            }
                        }

                        override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {

                            Timber.d("Fragment stopped: ${f::class.simpleName}")
                            f.executeIfBase { fr ->
                                offer(
                                    FragmentViewState(
                                        host = fr,
                                        view = null,
                                        state = ViewState.Stopped,
                                        key = fr.viewIdKey
                                    )
                                )
                            }
                        }

                        override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {

                            f.executeIfBase { fr ->

                                Timber.d("Fragment destroyed: ${f::class.simpleName} — ${fr.viewIdKey}")
                                offer(
                                    FragmentViewState(
                                        host = fr,
                                        view = null,
                                        state = ViewState.Destroyed,
                                        key = fr.viewIdKey
                                    )
                                )

                                val count = refCount[fr::class]?.decrementAndGet()

                                Timber.i("Fragment destroyed: ${f::class.simpleName} — ${fr.viewIdKey} ${fr.requireActivity().isFinishing}, ${fr.isRemoving}, ${!fr.requireActivity().isChangingConfigurations}")
                                Timber.i("Fragment destroyed: ${f::class.simpleName} — overall ${(fr.requireActivity().isFinishing || fr.isRemoving) && !fr.requireActivity().isChangingConfigurations}")

                                if ((fr.requireActivity().isFinishing || fr.isRemoving) && !fr.requireActivity().isChangingConfigurations) {

                                    offer(
                                        FragmentViewState(
                                            host = fr,
                                            view = null,
                                            state = ViewState.Dead,
                                            key = fr.viewIdKey
                                        )
                                    )

                                    if (count == 0) {
                                        refCount.remove(fr::class)
                                    }

                                    die(fr)

                                    if (refCount.isEmpty()) {
                                        close()
                                    }
                                }
                            }
                        }
                    }

                    val fragmentManager = activityState.host.supportFragmentManager

                    fragmentManager.registerFragmentLifecycleCallbacks(callbacks, true)
                    awaitClose {
                        fragmentManager.unregisterFragmentLifecycleCallbacks(callbacks)
                    }
                }
            }
            .flowOn(Dispatchers.Main.immediate)
            .shareIn(scope = this, started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0))
    }

    private val hostCallbacks = rxFragmentCallbacks

    init {

        val job = launch(Dispatchers.Main.immediate) {
            hostCallbacks
                .collect {
                    Timber.d("Host rx: $it")
                }

            /*{
                Timber.e(it)
                if (BuildConfig.DEBUG) {
                    throw RuntimeException(it)
                }
            })*/
        }
    }

    private fun <VE : ViewEvents, M> die(host: BaseHost<VE, M>) {
        Timber.d("Die $host")

        CustomCache.remove(host.viewIdKey)

        PresenterCache.remove(host.viewIdKey).also {
            it.cancel()
        }
    }

    private fun Any.logForClass(name: String, log: String) {
        if (this::class.simpleName == name) {
            Timber.i("$name: $log")
        }
    }

    private fun <VE : ViewEvents, VS : AndroidViewState<VE, M>, M> bindPresenter(
        host: BaseHost<VE, M>,
        savedInstanceState: Bundle?,
        hostEvents: Flow<VS>,
        args: Bundle?
    ): String {

        val viewKey = savedInstanceState?.getString(ViewDelegate.VIEW_ID_KEY)

        host.logForClass("SearchFragment", PresenterCache.log())
        host.logForClass("SearchFragment", "Search presenter: $viewKey")

        if (viewKey != null) { // Restore previous state

            host.viewIdKey = viewKey
            val presenter = if (PresenterCache.hasPresenter(viewKey)) { // Configuration changed
                host.logForClass("SearchFragment", "Has key, restore")
                PresenterCache.getPresenter(viewKey)
            } else {    // Process recreation
                host.logForClass("SearchFragment", "Has key, create new presenter")
                createPresenter(host, viewKey, savedInstanceState, hostEvents, args).also {
                    PresenterCache.putPresenter(viewKey, it)
                }
            }

            host.presenter = presenter
            return viewKey
        } else { // Create new, first launch
            host.logForClass("SearchFragment", "No key, create new presenter")
            val viewIdKey = UUID.randomUUID().toString()
            host.viewIdKey = viewIdKey

            createPresenter(host, viewIdKey, savedInstanceState, hostEvents, args).also {
                PresenterCache.putPresenter(viewIdKey, it)
                host.presenter = it
            }
            return viewIdKey
        }
    }

    private fun <VE : ViewEvents, VS : AndroidViewState<VE, M>, M> createPresenter(
        host: BaseHost<VE, M>,
        hostKey: String,
        savedInstanceState: Bundle?,
        hostEvents: Flow<VS>,
        args: Bundle?
    ): BasePresenter {

        val hostName = host::class.simpleName


        // val presenterScope = MainScope()
        val presenterScope = object : CoroutineScope {
            override val coroutineContext: CoroutineContext =
                SupervisorJob() + Dispatchers.Main.immediate
        }

        val shareScope = presenterScope
        // val shareScope = this

        val state = hostEvents
            .filter { it.key == hostKey }
            .map { it as AndroidViewState<VE, M> }
            .runningReduce { previous, current -> current.reduce(previous) }
            .takeUntil { it.state == ViewState.Dead }
            .flowOn(Dispatchers.Main.immediate)
            .shareIn(scope = shareScope, started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0))

        val restoredState = state
            .filter { it.state == ViewState.Created || it.state == ViewState.Destroyed }
            .map {
                if (it.restoredState != null) {
                    Optional(it.restoredState)
                } else {
                    Optional(null)
                }
            }
            .distinctUntilChanged()
            .map { it.value }
            .flowOn(Dispatchers.Main.immediate)
            .shareIn(scope = shareScope, started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0), replay = 1)

        val outState = state
            .map {
                if (it.outState == null) {
                    Optional(null)
                } else {
                    Optional(it.outState)
                }
            }
            .distinctUntilChanged()
            .map { it.value }
            .flowOn(Dispatchers.Main.immediate)
            .shareIn(scope = shareScope, started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0), replay = 1)

        val hostState = state
            .map { it.state }
            .flowOn(Dispatchers.Main.immediate)
            .shareIn(scope = shareScope, started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0), replay = 1)

        val viewState = state
            // .flowOn(Dispatchers.Main.immediate)
            .shareIn(scope = shareScope, started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0), replay = 1)

        val viewEvents = viewState
            .distinctUntilChanged { prev, current ->
                sameAliveState(prev, current) && prev.view == current.view
            }
            .map { vs ->
                if (vs.state.isAlive && vs.view != null) {
                    vs.host.createViewHolder(vs.view!!)
                } else {
                    null
                }
            }
            .flowOn(Dispatchers.Main.immediate)
            .shareIn(scope = shareScope, started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0), replay = 1)

        val renderer = viewState
            .distinctUntilChanged(::sameAliveState)
            .map { vs ->
                if (vs.state.isAlive) {
                    vs.host
                } else {
                    null
                }
            }
            .flowOn(Dispatchers.Main.immediate)
            .shareIn(scope = shareScope, started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0), replay = 1)

        val viewChannel = ViewChannel(
            restoredState = restoredState,
            outState = outState,
            state = hostState,
            viewEvents = viewEvents,
            renderer = renderer
        )

        Timber.i("++++++++++ launch-$hostName collectors")

        presenterScope.launch(Dispatchers.Main.immediate) {
            restoredState.collect {
                Timber.i("++++++++++ next restoredState-$hostName $it")
            }
        }
        Timber.i("++++++++++ restoredState-$hostName done")

        presenterScope.launch(Dispatchers.Main.immediate) {
            outState.collect {
                Timber.i("++++++++++ next outState-$hostName $it")
            }
        }
        Timber.i("++++++++++ outState-$hostName done")

        presenterScope.launch(Dispatchers.Main.immediate) {
            state.collect {
                Timber.i("++++++++++ next state-$hostName $it, ${it.state.name}")
            }
        }
        Timber.i("++++++++++ state-$hostName done")

        presenterScope.launch(Dispatchers.Main.immediate) {
            viewEvents.collect {
                val name = it?.let { it::class.simpleName } ?: "null"
                Timber.i("++++++++++ next viewEvents-$hostName $it")
            }
        }
        Timber.i("++++++++++ viewEvents-$hostName done")

        presenterScope.launch(Dispatchers.Main.immediate) {
            renderer.collect {
                val name = it?.let { it::class.simpleName } ?: "null"
                Timber.i("++++++++++ next renderer-$hostName $it")
            }
        }
        Timber.i("++++++++++ renderer-$hostName done")

        return host.createPresenter(viewChannel, args, savedInstanceState, presenterScope)
    }
}

@ExperimentalCoroutinesApi
fun <T> Observable<T>.asFlow(): Flow<T> = callbackFlow {
    val disposable = subscribe({
        offer(it)
    }, {
        close(it)
    }, {
        close()
    })

    awaitClose {
        disposable.dispose()
    }
}

@ExperimentalCoroutinesApi
fun <T> Observable<T>.asFlow(tag: String): Flow<T> = callbackFlow {
    Timber.i("++++++++++ $tag start")
    val disposable = subscribe({
        Timber.i("++++++++++ $tag offer next")
        offer(it)
    }, {
        Timber.i("++++++++++ $tag offer error")
        close(it)
    }, {
        Timber.i("++++++++++ $tag offer complete")
        close()
    })

    Timber.i("++++++++++ $tag waiting")
    awaitClose {
        Timber.i("++++++++++ $tag close")
        disposable.dispose()
    }
}

// TODO: Make proper cancellation
fun <T> Flow<T>.takeUntil(predicate: suspend (T) -> Boolean): Flow<T> = flow {
    try {
        collect { value ->
            emit(value)
            if (predicate(value)) {
                throw CancellationException()
            }
        }
    } catch (e: CancellationException) {
        // TODO check owner
        // e.checkOwnership(owner = this)
    }
}

fun <T1, T2> Flow<T1>.takeUntil(other: Flow<T2>): Flow<T1> =
    object : AbstractFlow<T1>() {
        override suspend fun collectSafely(collector: FlowCollector<T1>) {
            coroutineScope {

                val lock = AtomicBoolean(false)
                val job = launch {
                    other.first()
                    lock.compareAndSet(false, true)
                }

                this@takeUntil.collect {
                    if (!lock.get()) {
                        collector.emit(it)
                    } else {
                        collector.emit(it)
                        job.cancel()
                    }
                }
            }
        }
    }