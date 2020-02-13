package io.truereactive.library.rx.abstraction

import android.os.Bundle
import io.reactivex.Observable
import io.truereactive.library.core.Optional
import io.truereactive.library.core.Renderer
import io.truereactive.library.core.ViewEvents
import io.truereactive.library.core.ViewState

/**
 * A reactive representation of a view.
 * @param outState saved state
 * @param state an observable view state (Created, Resumed, Paused, etc.). Emits current state upon subscription.
 * @param viewEvents a stream of events from view like text input, button clicks, etc.
 * @param renderer a instance which renders desired data.
 */
data class ViewChannel<VE : ViewEvents, M>(
    internal val restoredState: Observable<Optional<out Bundle>>,
    internal val outState: Observable<Optional<out Bundle>>,
    val state: Observable<ViewState>,
    internal val viewEvents: Observable<Optional<out VE>>,
    internal val renderer: Observable<Optional<out Renderer<M>>> // TODO use weakRef?
)