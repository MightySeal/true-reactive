package io.truereactive.library.flow

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.truereactive.library.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

interface ReactiveApplication {
}


// TODO: Use JB share implementation
//  https://github.com/Kotlin/kotlinx.coroutines/issues/1261
//  https://github.com/Kotlin/kotlinx.coroutines/issues/1086
@ExperimentalCoroutinesApi
class ReactiveApp(app: Application) : ReactiveApplication, CoroutineScope by MainScope() {

    /*val optionalReporter = ObjectInspector { reporter ->
        reporter.whenInstanceOf(Optional::class) { instance ->

            val label = instance[Optional::class, "logKey"]!!
            reporter.labels.add("${label.name}: ${label.value.readAsJavaString()}")
        }
    }*/

    // private val compositeDisposable = CompositeDisposable()
    private val appJob = Job()

    // TODO: Use reduce like in fragment
    // TODO: possibly dispose when activity count is 0. Maybe sort of refCount base on activity count
    private val rxActivityCallbacks: Flow<ActivityViewState<ViewEvents, Any>> by lazy(
        LazyThreadSafetyMode.NONE::class
    ) {

        // Observable.create<ActivityViewState<ViewEvents, Any>> { emitter ->
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
                Timber.d("Unregister activity callbacks")
                app.unregisterActivityLifecycleCallbacks(callbacks)
            }
        }
        //.share()
    }

    private val rxFragmentCallbacks: Flow<FragmentViewState<ViewEvents, Any>> by lazy(
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

                callbackFlow {
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
                                        // emitter.onComplete()
                                        cancel()
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
                    .distinctUntilChanged()
                    .scanReduce { first, second ->
                        when (second.state) {
                            ViewState.Created -> second

                            ViewState.Started,
                            ViewState.Resumed,
                            ViewState.Paused -> second.copy(view = first.view)

                            ViewState.Stopped,
                            ViewState.Destroyed,
                            ViewState.SavingState,
                            ViewState.Dead -> second.copy(view = null)
                        }
                    }
                // .share()
            }
        // .share()
    }

    private val hostCallbacks = rxFragmentCallbacks

    init {

        // TODO: handle errors
        launch(appJob) {
            hostCallbacks
                .collect {

                }
        }
    }

    private fun <VE : ViewEvents, M> die(host: BaseHost<VE, M>) {
        Timber.d("Die $host")

        CustomCache.remove(host.viewIdKey)

        PresenterCache.remove(host.viewIdKey).also {
            Timber.d("Dispose ${it.parentJob.children.count()} elements")
            it.dispose()
        }
    }

    private fun <VE : ViewEvents, VS : AndroidViewState<VE, M>, M> bindPresenter(
        host: BaseHost<VE, M>,
        savedInstanceState: Bundle?,
        hostEvents: Flow<VS>,
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
        hostEvents: Flow<VS>,
        args: Bundle?
    ): BasePresenter {

        // TODO: Make state for for internal and external use. Internal has SavingState, external doesn't.
        val state = hostEvents
            .filter { it.key == hostKey }
            .takeWhile { it.state != ViewState.Dead }
        // .takeUntil { it.state == ViewState.Dead }
        // .share()

        val restoredState = state
            .filter { it.state == ViewState.Created || it.state == ViewState.Destroyed }
            .map { it.restoredState }
            .distinctUntilChanged()
        // .replay(1)

        val outState = state
            .distinctUntilChanged()
            .map { it.outState }
        // .replay(1)

        val hostState = state
            .map { it.state }
        // .replay(1)

        val viewState = state
        // .replay(1)

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
        // .replay(1)

        val renderer = viewState
            .distinctUntilChanged(::sameAliveState)
            .map { vs ->
                if (vs.state.isAlive) {
                    vs.host
                } else {
                    null
                }
            }
        // .replay(1)

        val viewChannel = ViewChannel(
            restoredState = restoredState,
            outState = outState,
            state = hostState,// .observeOn(Schedulers.computation()),
            viewEvents = viewEvents,
            renderer = renderer
        )

        val restoredStateDisposable = restoredState.connect()
        val savedStateDisposable = outState.connect()
        val hostStateDisposable = hostState.connect()
        val viewStateDisposable = viewState.connect()
        val viewEventsDisposable = viewEvents.connect()
        val rendererDisposable = renderer.connect()

        return host.createPresenter(viewChannel, args, savedInstanceState).also {
            it.disposable.add(restoredStateDisposable)
            it.disposable.add(savedStateDisposable)
            it.disposable.add(hostStateDisposable)
            it.disposable.add(viewStateDisposable)
            it.disposable.add(viewEventsDisposable)
            it.disposable.add(rendererDisposable)
        }
    }
}

private class SharedFlow : SafeFlow