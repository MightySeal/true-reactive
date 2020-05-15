package io.truereactive.library.rx.reactiveui

import android.os.Bundle
import android.view.View
import io.truereactive.library.core.ViewEvents
import io.truereactive.library.core.ViewState
import io.truereactive.library.rx.abstraction.BaseActivity
import io.truereactive.library.rx.abstraction.BaseFragment
import io.truereactive.library.rx.abstraction.BaseHost

internal abstract class AndroidViewState<VE : ViewEvents, M> {
    internal abstract val host: BaseHost<VE, M>
    internal abstract val outState: Bundle?
    internal abstract val restoredState: Bundle?
    abstract val view: View?
    abstract val state: ViewState
    abstract val key: String

    internal abstract fun reduce(previous: AndroidViewState<VE, M>): AndroidViewState<VE, M>
}

internal data class ActivityViewState<VE : ViewEvents, M>(
    override val host: BaseActivity<VE, M>,
    override val outState: Bundle? = null,
    override val restoredState: Bundle? = null,
    override val view: View?,
    override val state: ViewState,
    override val key: String
) : AndroidViewState<VE, M>() {

    override fun reduce(previous: AndroidViewState<VE, M>): AndroidViewState<VE, M> = this

    /*override fun reduce(previous: AndroidViewState<VE, M>): AndroidViewState<VE, M> {
        return when (this.state) {
            ViewState.Created -> this

            ViewState.Started,
            ViewState.Resumed,
            ViewState.Paused -> this.copy(view = previous.view)

            ViewState.Stopped,
            ViewState.Destroyed,
            ViewState.SavingState,
            ViewState.Dead -> this.copy(view = null)
        }
    }*/
}

internal data class FragmentViewState<VE : ViewEvents, M>(
    override val host: BaseFragment<VE, M>,
    override val outState: Bundle? = null,
    override val restoredState: Bundle? = null,
    override val view: View?,
    override val state: ViewState,
    override val key: String
) : AndroidViewState<VE, M>() {

    override fun reduce(previous: AndroidViewState<VE, M>): AndroidViewState<VE, M> {
        return when (this.state) {
            ViewState.Created -> this

            ViewState.Started,
            ViewState.Resumed,
            ViewState.Paused -> this.copy(view = previous.view)

            ViewState.Stopped,
            ViewState.Destroyed,
            ViewState.SavingState,
            ViewState.Dead -> this.copy(view = null)
        }
    }
}

internal fun <VE : ViewEvents, M, AVS : AndroidViewState<VE, M>> sameAliveState(
    first: AVS,
    second: AVS
): Boolean =
    io.truereactive.library.core.sameAliveState(first.state, second.state)

internal fun <VE : ViewEvents, M> FragmentViewState<VE, M>.print(): String =
    "${this::class.simpleName}[host=${host::class.simpleName}, state=${state.name}, view=${view.hashCode()}]"