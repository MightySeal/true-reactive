package io.truereactive.library.rx.reactiveui

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.truereactive.library.core.*
import io.truereactive.library.rx.abstraction.BaseActivity
import io.truereactive.library.rx.abstraction.BaseFragment
import io.truereactive.library.rx.abstraction.ViewChannel
import timber.log.Timber

// TODO: provide a context for it (multireceivers could help a lot here)
//   interface ViewScope {
//      <VE : ViewEvents, M, D>.doSomething()
//   }
interface ViewScope {

    fun <VE : ViewEvents, M> Observable<M>.renderWhileAlive(channel: ViewChannel<VE, M>): Disposable {
        return Observable.combineLatest(
            channel.renderer,
            this,
            BiFunction { renderer: Optional<out Renderer<M>>, data: M ->
                Pair(renderer, data)
            }
        ).takeUntil(channel.state.filter { it == ViewState.Dead })
            .filter { it.first.value != null }
            .map { Pair(it.first.value!!, it.second) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { (renderer, data) ->
                renderer.render(data)
            }
    }

}

// TODO: due to strict sync requirement observeOn operators can break this thing.
fun <VE : ViewEvents, M, D> Observable<D>.saveState(
    channel: ViewChannel<VE, M>,
    invoke: (Bundle, D) -> Unit
): Disposable {

    return Observable.combineLatest(
        channel.outState,
        this,
        BiFunction { bundle: Optional<out Bundle>, data: D ->
            Pair(bundle, data)
        }
    ).takeUntil(channel.state.filter { it == ViewState.Dead })
        .filter { values: Pair<Optional<out Bundle>, D> ->
            values.first.value != null
        }.map {
            Pair(it.first.value!!, it.second)
        }.subscribe { (bundle, data) ->
            invoke(bundle, data)
        }
}

fun <VE : ViewEvents, M> ViewChannel<VE, M>.restoredState(): Observable<Bundle> {
    return this.restoredState
        .filter { it.value != null }
        .map { it.value!! }
        .observeOn(Schedulers.computation())
        .takeUntil(this.state.filter { it == ViewState.Dead })
}

fun <VE : ViewEvents, M> Observable<M>.renderWhileActive(channel: ViewChannel<VE, M>): Disposable {
    val dataObservable = channel.state
        .switchMap { event ->
            if (event.isAlive) {
                this
            } else {
                Observable.empty()
            }
        }
    return Observable.combineLatest(
        channel.renderer,
        dataObservable,
        BiFunction<Optional<out Renderer<M>>, M, Pair<Optional<out Renderer<M>>, M>> { renderer, data ->
            Pair(renderer, data)
        }
    ).takeUntil(channel.state.filter { it == ViewState.Dead })
        .filter { values: Pair<Optional<out Renderer<M>>, M> ->
            values.first.value != null
        }.map {
            Pair(it.first.value!!, it.second)
        }.observeOn(AndroidSchedulers.mainThread())
        .subscribe { (renderer, data) ->
            renderer.render(data)
        }
}

/**
 *
 *  Renders this observable when the view is ready and until the view is permanently destroyed.
 *
 *
 *  @param channel ViewChannel for this view
 *  @return Disposable for this stream
 *
 */
fun <VE : ViewEvents, M> Observable<M>.renderWhileAlive(channel: ViewChannel<VE, M>): Disposable {
    return Observable.combineLatest(
        channel.renderer,
        this,
        BiFunction { renderer: Optional<out Renderer<M>>, data: M ->
            Pair(renderer, data)
        }
    ).takeUntil(channel.state.filter { it == ViewState.Dead })
        .filter { it.first.value != null }
        .map { Pair(it.first.value!!, it.second) }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { (renderer, data) ->
            renderer.render(data)
        }
}

// TODO: Consider one-shot rendering
// TODO: Consider making it vice-versa `ViewChannel<VE, M>.renderOnce(data: M)`
/**
 * Render this data model when the view is ready.
 *
 * @param channel ViewChannel for this view
 * @return Disposable for this stream
 */
fun <VE : ViewEvents, M> M.renderWhileAlive(channel: ViewChannel<VE, M>): Disposable {
    return channel.renderer
        .takeUntil(channel.state.filter { it == ViewState.Dead })
        .filter { it.value != null }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { renderer ->
            renderer.value?.render(this)
        }
}


// TODO: REVIEW, maybe need to switch to empty observable when the value is null
/**
 *
 *  Creates a stream of events of type T which is completed when the view is destroyed permanently.
 *  @param selector selector for desired event stream
 *  @return Observable<T> of type T that reflects selected stream across any config changes, recreations, except process death.
 *
 */
fun <VE : ViewEvents, M, T> ViewChannel<VE, M>.viewEventsUntilDead(
    tag: String? = null,
    selector: VE.() -> Observable<T>
): Observable<T> {
    return this.viewEvents
        .observeOn(AndroidSchedulers.mainThread())
        .filter { it.value != null }
        .switchMap { selector(it.value!!) }
        .takeUntil(this.state.filter { it == ViewState.Dead })
}

fun <VE : ViewEvents, M, T> ViewChannel<VE, M>.mapUntilDead(block: VE.() -> T): Observable<T> {
    return this.viewEvents
        .filter { it.value != null }
        .map { block(it.value!!) }
        .observeOn(Schedulers.computation())
        .takeUntil(this.state.filter { it == ViewState.Dead })
}

internal inline fun Activity.executeIfBase(action: (BaseActivity<ViewEvents, Any>) -> Unit) {
    (this as? BaseActivity<ViewEvents, Any>)?.let(action)
}

internal inline fun Fragment.executeIfBase(action: (BaseFragment<ViewEvents, Any>) -> Unit) {
    // TODO: get rid of unchecked with some magic
    (this as? BaseFragment<ViewEvents, Any>)?.let(action)
}