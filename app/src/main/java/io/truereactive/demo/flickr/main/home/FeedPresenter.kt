package io.truereactive.demo.flickr.main.home

import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.renderWhileAlive
import io.truereactive.core.reactiveui.restoredState
import io.truereactive.core.reactiveui.saveState
import io.truereactive.core.reactiveui.viewEventsUntilDead

class FeedPresenter(
    private val channel: ViewChannel<FeedViewEvents, FeedState>
) : BasePresenter() {

    init {
        // TODO: make different sources instead of different queries (i.e. Flickr, Unsplash, Dribbble, etc)
        val sources = listOf(
            "London",
            "Zurich",
            "Copenhagen",
            "Paris",
            "Amsterdam"
        )

        channel.restoredState()
            .map {
                val index = it.getInt(SAVED_INDEX_KEY)
                FeedState(
                    sources = sources,
                    selectedPage = index,
                    restored = true
                )
            }
            .startWith(FeedState(sources))
            .distinctUntilChanged()
            .renderWhileAlive(channel)

        channel.viewEventsUntilDead {
            pageSelectedEvents
        }.saveState(channel) { bundle, data ->
            bundle.putInt(SAVED_INDEX_KEY, data)
        }
    }

    companion object {
        private const val SAVED_INDEX_KEY = "pager_index_key"
    }

}