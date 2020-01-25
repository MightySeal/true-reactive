package io.truereactive.library.flow

import android.os.Bundle
import android.view.View
import io.truereactive.library.core.ViewEvents
import io.truereactive.library.core.ViewState

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

internal fun <VE : ViewEvents, M, AVS : AndroidViewState<VE, M>> sameAliveState(
    first: AVS,
    second: AVS
): Boolean =
    io.truereactive.library.core.sameAliveState(first.state, second.state)