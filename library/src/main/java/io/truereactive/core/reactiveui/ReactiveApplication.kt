package io.truereactive.core.reactiveui

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.truereactive.core.abstraction.*
import io.truereactive.core.abstraction.Optional
import io.truereactive.library.BuildConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import java.util.*

interface ReactiveApplication {
}

@ExperimentalCoroutinesApi
class ReactiveApp(app: Application) : ReactiveApplication {

    private val compositeDisposable = CompositeDisposable()

    // TODO: Use reduce like in fragment
    // TODO: possibly dispose when activity count is 0. Maybe sort of refCount base on activity count
    private val rxActivityCallbacks: Observable<ActivityViewState<ViewEvents, Any>> by lazy(
        LazyThreadSafetyMode.NONE
    ) {

        Observable.create<ActivityViewState<ViewEvents, Any>> { emitter ->
            val callbacks = object : AbstractActivityCallbacks() {

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    activity.executeIfBase { baseActivity ->
                        bindPresenter(
                            baseActivity,
                            savedInstanceState,
                            rxActivityCallbacks,
                            baseActivity.intent.extras
                        )

                        emitter.onNext(
                            ActivityViewState(
                                host = baseActivity,
                                view = activity.window.decorView.rootView,
                                state = ViewState.Created,
                                key = baseActivity.viewIdKey
                            )
                        )
                    }
                }

                override fun onActivityStarted(activity: Activity) {
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

                    activity.executeIfBase { baseActivity ->
                        emitter.onNext(
                            ActivityViewState(
                                host = baseActivity,
                                view = baseActivity.window.decorView.rootView,
                                state = ViewState.Active,
                                key = baseActivity.viewIdKey
                            )
                        )
                    }
                }

                override fun onActivityPaused(activity: Activity) {
                    activity.executeIfBase { baseActivity ->
                        emitter.onNext(
                            ActivityViewState(
                                host = baseActivity,
                                view = baseActivity.window.decorView.rootView,
                                state = ViewState.Inactive,
                                key = baseActivity.viewIdKey
                            )
                        )
                    }
                }

                override fun onActivityStopped(activity: Activity) {
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
                    Timber.i("Activity destroyed: ${activity::class.simpleName}, isFinishing ${activity.isFinishing} changing config ${activity.isChangingConfigurations}")
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
                            die(baseActivity)
                            emitter.onNext(
                                ActivityViewState(
                                    host = baseActivity,
                                    view = null,
                                    state = ViewState.Dead,
                                    key = baseActivity.viewIdKey
                                )
                            )
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
                                savedInstanceState = outState,
                                state = ViewState.SavingState,
                                key = baseActivity.viewIdKey
                            )
                        )
                    }
                }

            }

            app.registerActivityLifecycleCallbacks(callbacks)

            emitter.setCancellable {
                Timber.i("Unregister activity callbacks")
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

                Observable.create<FragmentViewState<ViewEvents, Any>> { emitter ->
                    val callbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
                        override fun onFragmentPreCreated(
                            fm: FragmentManager,
                            f: Fragment,
                            savedInstanceState: Bundle?
                        ) {

                            // TODO: Check if this method is called when rotated
                            Timber.i("${f::class.simpleName}:${f.hashCode()} onFragmentPreCreated")
                            f.executeIfBase { fragment ->
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
                                emitter.onNext(
                                    FragmentViewState(
                                        host = it,
                                        view = v,
                                        state = ViewState.Created,
                                        key = it.viewIdKey
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
                                        savedInstanceState = outState,
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
                                        state = ViewState.Active,
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
                                        state = ViewState.Inactive,
                                        key = it.viewIdKey
                                    )
                                )
                            }
                        }

                        override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
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

                            Timber.i("Fragment destroyed: ${f::class.simpleName}")
                            f.executeIfBase { fr ->

                                Timber.i("Fragment destroyed: ${fr::class.simpleName}, isFinishing ${fr.requireActivity().isFinishing} changing config ${fr.requireActivity().isChangingConfigurations} isRemoving: ${fr.isRemoving}")

                                emitter.onNext(
                                    FragmentViewState(
                                        host = fr,
                                        view = null,
                                        state = ViewState.Destroyed,
                                        key = fr.viewIdKey
                                    )
                                )

                                if ((fr.requireActivity().isFinishing || fr.isRemoving) && !fr.requireActivity().isChangingConfigurations) {
                                    die(fr)
                                    emitter.onNext(
                                        FragmentViewState(
                                            host = fr,
                                            view = null,
                                            state = ViewState.Dead,
                                            key = fr.viewIdKey
                                        )
                                    )
                                }
                            }
                        }
                    }

                    val fragmentManager = activityState.host.supportFragmentManager

                    fragmentManager.registerFragmentLifecycleCallbacks(callbacks, true)
                    emitter.setCancellable {
                        fragmentManager.unregisterFragmentLifecycleCallbacks(callbacks)
                    }
                }.distinctUntilChanged()
                    .scan { first, second ->

                        when (second.state) {
                            ViewState.Created -> second

                            ViewState.Started,
                            ViewState.Active,
                            ViewState.Inactive -> second.copy(view = first.view)

                            ViewState.Stopped,
                            ViewState.Destroyed,
                            ViewState.SavingState,
                            ViewState.Dead -> second.copy(view = null)
                        }
                    }
                    .takeUntil { it.state == ViewState.Dead && it.host.activity?.isFinishing ?: false }
                    .share()
            }
            .share()
    }

    private val hostCallbacks = rxFragmentCallbacks

    init {
        hostCallbacks
            .subscribe({
                Timber.d("Host rx: $it")
            }, {
                Timber.e(it)
                if (BuildConfig.DEBUG) {
                    throw RuntimeException(it)
                }
            }).let(compositeDisposable::add)
    }

    private fun <VE : ViewEvents, M> die(host: BaseHost<VE, M>) {
        Timber.i("Die $host")
        PresenterCache.remove(host.viewIdKey).also {
            it.dispose()
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
            val presenter = if (PresenterCache.hasPresenter(viewKey)) { // Configuration changed
                PresenterCache.getPresenter(viewKey)
            } else {    // Process recreation
                createPresenter(host, viewKey, savedInstanceState, hostEvents, args).also {
                    PresenterCache.putPresenter(viewKey, it)
                }
            }

            host.viewIdKey = viewKey
            host.presenter = presenter
            return viewKey
        } else { // Create new, first launch
            val viewIdKey = UUID.randomUUID().toString()

            createPresenter(host, viewIdKey, savedInstanceState, hostEvents, args).also {
                PresenterCache.putPresenter(viewIdKey, it)
                host.presenter = it
                host.viewIdKey = viewIdKey
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
            .filter { it.key == hostKey }
            .share()

        val savedState = state
            .filter { it.savedInstanceState != null }
            .map { it.savedInstanceState!! }
            .replay(1)

        val hostState = state.map { it.state }
            .replay(1)

        // TODO: rethink filtering logic
        val viewState = state
            .takeWhile { it.state != ViewState.Dead }
            .replay(1)

        // TODO: rethink filtering logic
        val viewEvents = viewState
            .filter { it.view != null }
            .map { it.host.createViewHolder(it.view!!) }
            .replay(1)

        val renderer = viewState
            .map {
                if (it.state.isAlive) {
                    Optional(it.host)
                } else {
                    Optional<Renderer<M>>(null)
                }
            }
            .replay(1)

        val viewChannel = ViewChannel(savedState, hostState, viewEvents, renderer)

        val savedStateDisposable = savedState.connect()
        val hostStateDisposable = hostState.connect()
        val viewStateDisposable = viewState.connect()
        val viewEventsDisposable = viewEvents.connect()
        val rendererDisposable = renderer.connect()

        return host.createPresenter(viewChannel, args, savedInstanceState).also {
            it.disposable.add(savedStateDisposable)
            it.disposable.add(hostStateDisposable)
            it.disposable.add(viewStateDisposable)
            it.disposable.add(viewEventsDisposable)
            it.disposable.add(rendererDisposable)
        }
    }
}