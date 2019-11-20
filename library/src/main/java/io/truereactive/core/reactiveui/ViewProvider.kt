package io.truereactive.core.reactiveui

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.truereactive.core.abstraction.*
import timber.log.Timber

interface ViewEvents

// TODO: not working
//   Needed only in case of process death
fun <VE : ViewEvents, M, D> Observable<D>.saveState(
    channel: ViewChannel<VE, M>,
    invoke: (Bundle, D) -> Unit
): Disposable {

    return Observable.combineLatest(
        channel.state,
        channel.savedState,
        this,
        Function3 { state: ViewState, bundle: Bundle, data: D ->
            Triple(state, bundle, data)
        }
    ).takeUntil { it.first == ViewState.Dead }
        .firstElement()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { (viewState, bundle, data) ->
            Timber.i("Save state $data, state: $viewState")
            invoke(bundle, data)
        }
}

fun <VE : ViewEvents, M> Observable<M>.renderWhileActive(channel: ViewChannel<VE, M>): Disposable {
    // TODO: distinct somehow (ViewState.isAlive)
    val dataObservable = channel.state
        .switchMap { event ->
            if (event in listOf(ViewState.Created, ViewState.Started, ViewState.Active)) {
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
    ).filter { values: Pair<Optional<out Renderer<M>>, M> ->
        values.first.value != null
    }.map {
        Pair(it.first.value!!, it.second)
    }.observeOn(AndroidSchedulers.mainThread())
        .subscribe { (renderer, data) ->
            renderer.render(data)
        }
}

fun <VE : ViewEvents, M> Observable<M>.renderWhileAlive(channel: ViewChannel<VE, M>): Disposable {
    return Observable.combineLatest(
        channel.state,      // TODO: distinct somehow (ViewState.isAlive)
        channel.renderer,
        this,
        Function3 { state: ViewState, renderer: Optional<out Renderer<M>>, data: M ->
            Triple(state, renderer, data)
        }
    ).filter {
        it.first in listOf(
            ViewState.Created,
            ViewState.Started,
            ViewState.Active
        ) && it.second.value != null
    }
        .map {
            Triple(it.first, it.second.value!!, it.third)
        }.observeOn(AndroidSchedulers.mainThread())
        .subscribe { (_, renderer, data) ->
            renderer.render(data)
        }
}

fun <VE : ViewEvents, M, T> ViewChannel<VE, M>.viewEventsUntilDead(block: VE.() -> Observable<T>): Observable<T> {
    return this.viewEvents
        .switchMap(block)
        .takeUntil<T> { this.state.filter { it == ViewState.Dead } }
}

fun <VE : ViewEvents, M, T> ViewChannel<VE, M>.mapUntilDead(block: VE.() -> T): Observable<T> {
    return this.viewEvents
        .map(block)
        .takeUntil<T> { this.state.filter { it == ViewState.Dead } }
}

internal inline fun Activity.executeIfBase(action: (BaseActivity<ViewEvents, Any>) -> Unit) {
    (this as? BaseActivity<ViewEvents, Any>)?.let(action)
}

internal inline fun Fragment.executeIfBase(action: (BaseFragment<ViewEvents, Any>) -> Unit) {
    // TODO: get rid of unchecked with some magic
    (this as? BaseFragment<ViewEvents, Any>)?.let(action)
}