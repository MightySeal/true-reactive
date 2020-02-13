package io.truereactive.library.flow

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.truereactive.library.core.*
import io.truereactive.library.core.Optional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import shark.ObjectInspector
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
class ReactiveApplicationCompat(app: Application) : ReactiveApplication {

    val optionalReporter = ObjectInspector { reporter ->
        reporter.whenInstanceOf(Optional::class) { instance ->

            val label = instance[Optional::class, "logKey"]!!
            reporter.labels.add("${label.name}: ${label.value.readAsJavaString()}")
        }
    }

    private val compositeDisposable = CompositeDisposable()

    // TODO: Use reduce like in fragment
    // TODO: possibly dispose when activity count is 0. Maybe sort of refCount base on activity count
    private val rxActivityCallbacks: Observable<ActivityViewState<ViewEvents, Any>> by lazy(
        LazyThreadSafetyMode.NONE
    ) {

        Observable.create<ActivityViewState<ViewEvents, Any>> { emitter ->
            val callbacks = object : AbstractActivityCallbacks() {

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    Timber.i("========== onActivityCreated $activity")
                    activity.executeIfBase { baseActivity ->
                        bindPresenter(
                            baseActivity,
                            savedInstanceState,
                            rxActivityCallbacks
                                .doOnNext {
                                    Timber.i("========== onNext activity acllbacks $it")
                                }.doOnSubscribe {
                                    Timber.i("========== onSubscribe activity acllbacks")
                                },
                            baseActivity.intent.extras
                        )

                        emitter.onNext(
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
                    Timber.i("========== onActivityStarted $activity")
                    activity.executeIfBase { baseActivity ->
                        emitter.onNext(
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
                    Timber.i("========== onActivityResumed $activity")

                    activity.executeIfBase { baseActivity ->
                        emitter.onNext(
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
                    Timber.i("========== onActivityPaused $activity")
                    activity.executeIfBase { baseActivity ->
                        emitter.onNext(
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
                    Timber.i("========== onActivityStopped $activity")
                    activity.executeIfBase { baseActivity ->
                        emitter.onNext(
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

                        emitter.onNext(
                            ActivityViewState(
                                host = baseActivity,
                                view = null,
                                state = ViewState.Destroyed,
                                key = baseActivity.viewIdKey
                            )
                        )

                        if (activity.isFinishing && !activity.isChangingConfigurations) {
                            emitter.onNext(
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

                        emitter.onNext(
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

            emitter.setCancellable {
                Timber.d("Unregister activity callbacks")
                app.unregisterActivityLifecycleCallbacks(callbacks)
            }
        }.share()
    }

    private val rxFragmentCallbacks: Observable<FragmentViewState<ViewEvents, Any>> by lazy(
        LazyThreadSafetyMode.NONE
    ) {
        rxActivityCallbacks
            .filter { it.state == ViewState.Created }
            .switchMap { activityState ->

                // This hack is need to properly handle fragment destruction in some cases (i.e. viewpager).
                //  In this case fragment is destroyed *after* the parent activity is destroyed,
                //  so it's not possible to simply end this stream, we need to handle all the fragments destruction an complete only after that.
                // TODO: extend this behavior for any fragment, because it's good to have an option to add 3rd party fragments which are not BaseFragment
                val refCount =
                    mutableMapOf<KClass<out BaseFragment<ViewEvents, Any>>, AtomicInteger>()

                Observable.create<FragmentViewState<ViewEvents, Any>> { emitter ->
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
                                emitter.onNext(
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

                                emitter.onNext(
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
                                emitter.onNext(
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
                                emitter.onNext(
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
                                emitter.onNext(
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
                                emitter.onNext(
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
                                emitter.onNext(
                                    FragmentViewState(
                                        host = fr,
                                        view = null,
                                        state = ViewState.Destroyed,
                                        key = fr.viewIdKey
                                    )
                                )

                                val count = refCount[fr::class]?.decrementAndGet()

                                if ((fr.requireActivity().isFinishing || fr.isRemoving) && !fr.requireActivity().isChangingConfigurations) {

                                    emitter.onNext(
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
                                        emitter.onComplete()
                                    }
                                }
                            }
                        }
                    }

                    val fragmentManager = activityState.host.supportFragmentManager

                    fragmentManager.registerFragmentLifecycleCallbacks(callbacks, true)
                    emitter.setCancellable {
                        fragmentManager.unregisterFragmentLifecycleCallbacks(callbacks)
                    }
                }
            }
            .share()
    }

    private val hostCallbacks = rxFragmentCallbacks

    init {
        Timber.i("========== Init")

        hostCallbacks
            .doOnSubscribe {
                Timber.i("========== Subscribe for host callback")
            }.doOnNext {
                Timber.i("========== Next host callback $it")
            }
            .subscribe({
                // Timber.d("Host rx: $it")
            }, {
                Timber.e(it)
                if (BuildConfig.DEBUG) {
                    throw RuntimeException(it)
                }
            }).let(compositeDisposable::add)
    }

    private fun <VE : ViewEvents, M> die(host: BaseHost<VE, M>) {
        Timber.d("Die $host")

        CustomCache.remove(host.viewIdKey)

        PresenterCache.remove(host.viewIdKey).also {
            // Timber.d("Dispose ${it.disposable.size()} elements")
            // it.dispose()
            it.cancel()
        }
    }

    private fun <VE : ViewEvents, VS : AndroidViewState<VE, M>, M> bindPresenter(
        host: BaseHost<VE, M>,
        savedInstanceState: Bundle?,
        hostEvents: Observable<VS>,
        args: Bundle?
    ): String {

        val viewKey = savedInstanceState?.getString(ViewDelegate.VIEW_ID_KEY)

        if (viewKey != null) { // Restore previous state

            host.viewIdKey = viewKey
            val presenter = if (PresenterCache.hasPresenter(viewKey)) { // Configuration changed
                PresenterCache.getPresenter(viewKey)
            } else {    // Process recreation
                createPresenter(host, viewKey, savedInstanceState, hostEvents, args).also {
                    PresenterCache.putPresenter(viewKey, it)
                }
            }

            host.presenter = presenter
            return viewKey
        } else { // Create new, first launch
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
        hostEvents: Observable<VS>,
        args: Bundle?
    ): BasePresenter {

        // TODO: Make state for for internal and external use. Internal has SavingState, external doesn't.
        val state = hostEvents
            .doOnSubscribe { Timber.i("========== Subscribe for state") }
            .doOnNext { Timber.i("========== emit host events $it") }
            .filter { it.key == hostKey }
            .takeUntil { it.state == ViewState.Dead }
            .share()

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
            .replay(1)

        val outState = state
            .map {
                if (it.outState == null) {
                    Optional(null)
                } else {
                    Optional(it.outState)
                }
            }
            .distinctUntilChanged()
            .replay(1)

        val hostState = state
            .map { it.state }
            .replay(1)

        val viewState = state
            .replay(1)

        val viewEvents = viewState
            .distinctUntilChanged { prev, current ->
                sameAliveState(prev, current) && prev.view == current.view
            }
            .map { vs ->
                if (vs.state.isAlive && vs.view != null) {
                    Optional(vs.host.createViewHolder(vs.view!!))
                } else {
                    Optional(null)
                }
            }
            .doOnSubscribe { Timber.i("========== start view events observable") }
            .replay(1)

        val renderer = viewState
            .distinctUntilChanged(::sameAliveState)
            .map { vs ->
                if (vs.state.isAlive) {
                    Optional(vs.host)
                } else {
                    Optional<Renderer<M>>(null)
                }
            }
            .replay(1)

        // TODO: Use flow extensions
        val restoredStateFlow =
            restoredState.asFlow().map { it.value }.flowOn(Dispatchers.Main.immediate)
        val outStateFlow = outState.asFlow().map { it.value }.flowOn(Dispatchers.Main.immediate)
        // state = hostState.observeOn(Schedulers.computation()),
        val stateFlow = hostState.asFlow().flowOn(Dispatchers.Main.immediate)
        val viewEventsFlow = viewEvents.asFlow().map { it.value }.flowOn(Dispatchers.Main.immediate)
            .onEach { Timber.i("========== emit view events $it") }
            .onStart { Timber.i("========== staart view events") }
        val rendererFlow = renderer.asFlow().map { it.value }.flowOn(Dispatchers.Main.immediate)

        val viewChannel = ViewChannel(
            restoredState = restoredStateFlow,
            outState = outStateFlow,
            state = stateFlow,
            viewEvents = viewEventsFlow,
            renderer = rendererFlow
        )

        /*val restoredStateDisposable = restoredState.connect()
        val savedStateDisposable = outState.connect()
        val hostStateDisposable = hostState.connect()
        val viewStateDisposable = viewState.connect()
        val viewEventsDisposable = viewEvents.connect()
        val rendererDisposable = renderer.connect()*/

        return host.createPresenter(viewChannel, args, savedInstanceState).also { presenter ->
            Timber.i("========== Launch")
            presenter.launch { restoredStateFlow.collect {} }
            Timber.i("========== Launch 1")
            presenter.launch { outStateFlow.collect {} }
            Timber.i("========== Launch 2")
            presenter.launch { stateFlow.collect {} }
            Timber.i("========== Launch 3")
            presenter.launch { viewEventsFlow.collect {} }
            Timber.i("========== Launch 4")
            presenter.launch { rendererFlow.collect {} }
            Timber.i("========== Launch 5")

            /*it.disposable.add(restoredStateDisposable)
            it.disposable.add(savedStateDisposable)
            it.disposable.add(hostStateDisposable)
            it.disposable.add(viewStateDisposable)
            it.disposable.add(viewEventsDisposable)
            it.disposable.add(rendererDisposable)*/
        }
    }
}

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