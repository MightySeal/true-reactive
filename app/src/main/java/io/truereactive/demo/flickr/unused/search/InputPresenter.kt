package io.truereactive.demo.flickr.unused.search

import io.reactivex.functions.BiFunction
import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.logLifecycle
import io.truereactive.core.reactiveui.mapUntilDead
import io.truereactive.core.reactiveui.renderWhileAlive
import io.truereactive.core.reactiveui.viewEventsUntilDead
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
class InputPresenter @Inject constructor(
    viewChannel: ViewChannel<InputViewEvents, SearchModel>
) : BasePresenter() {

    init {
        viewChannel
            .viewEventsUntilDead { searchInput }
            .subscribe {
                Timber.i("Input $it")
            }.untilDead()

        val mirror = viewChannel
            .viewEventsUntilDead { searchInput }
            .map { SearchModel(it.toString()) }

        mirror.renderWhileAlive(viewChannel)
            .untilDead()

        viewChannel
            .viewEventsUntilDead { searchInput }
            .distinctUntilChanged()
            .filter { it.length == 4 }
            .withLatestFrom(viewChannel.mapUntilDead { openDetails },
                BiFunction { text: String, openDetails: (String) -> Unit ->
                    Pair(
                        openDetails,
                        text
                    )
                }
            )
            .logLifecycle("==== Search navigation")
            .subscribe({
                it.first(it.second)
            }, {
                Timber.e(it)
            }).untilDead()
    }
}