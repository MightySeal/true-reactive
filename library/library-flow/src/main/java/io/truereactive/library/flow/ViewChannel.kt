package io.truereactive.library.flow

import android.os.Bundle
import io.truereactive.library.core.Renderer
import io.truereactive.library.core.ViewEvents
import io.truereactive.library.core.ViewState
import kotlinx.coroutines.flow.Flow

/**
 * A reactive representation of a view.
 * @param outState saved state
 * @param state an observable view state (Created, Resumed, Paused, etc.). Emits current state upon subscription.
 * @param viewEvents a stream of events from view like text input, button clicks, etc.
 * @param renderer a instance which renders desired data.
 */
data class ViewChannel<VE : ViewEvents, M>(
    internal val restoredState: Flow<Bundle?>,
    internal val outState: Flow<Bundle?>,
    val state: Flow<ViewState>,
    internal val viewEvents: Flow<VE?>,
    internal val renderer: Flow<Renderer<M>?> // TODO use weakRef?
)

