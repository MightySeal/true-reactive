package io.truereactive.library.flow

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import hu.akarnokd.kotlin.flow.takeUntil
import io.truereactive.library.core.ViewEvents
import io.truereactive.library.core.ViewState
import kotlinx.coroutines.flow.*

internal inline fun Activity.executeIfBase(action: (BaseActivity<ViewEvents, Any>) -> Unit) {
    (this as? BaseActivity<ViewEvents, Any>)?.let(action)
}

internal inline fun Fragment.executeIfBase(action: (BaseFragment<ViewEvents, Any>) -> Unit) {
    // TODO: get rid of unchecked with some magic
    (this as? BaseFragment<ViewEvents, Any>)?.let(action)
}

fun <VE : ViewEvents, M> ViewChannel<VE, M>.restoredState(): Flow<Bundle> {
    return this.restoredState
        .filterNotNull()
        .takeUntil(this.state.filter { it == ViewState.Dead })
}

suspend fun <VE : ViewEvents, M, D> Flow<D>.saveState(
    channel: ViewChannel<VE, M>,
    invoke: (Bundle, D) -> Unit
) = combine(
    channel.outState,
    this,
    { bundle, data -> bundle to data }
).filter { (bundle, data) ->
    bundle != null
}.collect { (bundle, data) ->
    invoke(bundle!!, data)
}

fun <VE : ViewEvents, M, T> ViewChannel<VE, M>.viewEventsUntilDead(selector: VE.() -> Flow<T>): Flow<T> {
    return this.viewEvents
        .filterNotNull()
        .flatMapLatest { selector(it) }
        .takeUntil(this.state.filter { it == ViewState.Dead })
}

suspend fun <VE : ViewEvents, M> Flow<M>.renderWhileAlive(channel: ViewChannel<VE, M>) {
    combine(
        channel.renderer,
        this,
        { renderer, data -> renderer to data }
    ).filter { (renderer, data) ->
        renderer != null
    }.collect { (renderer, data) ->
        renderer!!.render(data)
    }
}

fun <VE : ViewEvents, M, T> ViewChannel<VE, M>.mapUntilDead(block: VE.() -> T): Flow<T> =
    this.viewEvents
        .filterNotNull()
        .map { block(it) }
        .takeUntil(this.state.filter { it == ViewState.Dead })