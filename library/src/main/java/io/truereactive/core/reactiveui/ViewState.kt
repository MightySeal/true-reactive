package io.truereactive.core.reactiveui

import android.os.Bundle
import android.view.View
import io.truereactive.core.abstraction.BaseActivity
import io.truereactive.core.abstraction.BaseFragment
import io.truereactive.core.abstraction.BaseHost

internal abstract class AndroidViewState<VE : ViewEvents, M> {
    internal abstract val host: BaseHost<VE, M>
    internal abstract val outState: Bundle?
    internal abstract val restoredState: Bundle?
    abstract val view: View?
    abstract val state: ViewState
    abstract val key: String
}

internal data class ActivityViewState<VE : ViewEvents, M>(
    override val host: BaseActivity<VE, M>,
    override val outState: Bundle? = null,
    override val restoredState: Bundle? = null,
    override val view: View?,
    override val state: ViewState,
    override val key: String
) : AndroidViewState<VE, M>()

internal data class FragmentViewState<VE : ViewEvents, M>(
    override val host: BaseFragment<VE, M>,
    override val outState: Bundle? = null,
    override val restoredState: Bundle? = null,
    override val view: View?,
    override val state: ViewState,
    override val key: String
) : AndroidViewState<VE, M>()

sealed class ViewState(val name: String) {
    object Created : ViewState("Created")
    object Started : ViewState("Started")
    object Resumed : ViewState("Resumed")
    object Paused : ViewState("Paused")
    object Stopped : ViewState("Stopped")
    object SavingState : ViewState("SavingState")
    object Destroyed : ViewState("SavingState")
    object Dead : ViewState("Dead")
}

val ViewState.isAlive: Boolean
    get() = when (this) {
        ViewState.Created,
        ViewState.Started,
        ViewState.Resumed -> true

        ViewState.Paused,
        ViewState.Stopped,
        ViewState.SavingState,
        ViewState.Destroyed,
        ViewState.Dead -> false
    }

internal fun sameAliveState(first: ViewState, second: ViewState): Boolean =
    !(first.isAlive xor second.isAlive)

internal fun <VE : ViewEvents, M, AVS : AndroidViewState<VE, M>> sameAliveState(
    first: AVS,
    second: AVS
): Boolean =
    sameAliveState(first.state, second.state)

internal fun <VE : ViewEvents, M> FragmentViewState<VE, M>.print(): String =
    "${this::class.simpleName}[host=${host::class.simpleName}, state=${state.name}, view=${view.hashCode()}]"