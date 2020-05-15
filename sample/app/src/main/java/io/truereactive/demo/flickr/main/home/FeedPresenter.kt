package io.truereactive.demo.flickr.main.home

import io.truereactive.library.rx.abstraction.BasePresenter
import io.truereactive.library.rx.abstraction.ViewChannel
import io.truereactive.library.rx.reactiveui.renderWhileAlive
import io.truereactive.library.rx.reactiveui.restoredState
import io.truereactive.library.rx.reactiveui.saveState
import io.truereactive.library.rx.reactiveui.viewEventsUntilDead
import timber.log.Timber

class FeedPresenter(
    private val channel: ViewChannel<FeedViewEvents, FeedState>
) : BasePresenter() {

    init {
        // TODO: make different sources instead of different queries (i.e. Flickr, Unsplash, Dribbble, etc)
        val sources = listOf(
            "London",
            /*"Zurich",
            "Copenhagen",
            "Paris",
            "Amsterdam"*/
        )

        val restoredState = channel.restoredState()
            .filter { it.containsKey(SAVED_INDEX_KEY) }
            .map {
                it.getInt(SAVED_INDEX_KEY)
            }.share()

        restoredState
            .map { index ->
                FeedState(
                    sources = sources,
                    selectedPage = index,
                    restored = true
                )
            }
            .startWith(FeedState(sources))
            .distinctUntilChanged()
            // .renderWhileAlive(channel, ViewState.Resumed)
            .renderWhileAlive(channel)

        channel.viewEventsUntilDead {
            pageSelectedEvents
        }.saveState(channel) { bundle, data ->
            bundle.putInt(SAVED_INDEX_KEY, data)
        }

        channel.viewEventsUntilDead("Input") { searchInput }
            .subscribe { Timber.i("++++++++++ input $it") }
            .untilDead()
    }

    companion object {
        private const val SAVED_INDEX_KEY = "pager_index_key"
    }

}