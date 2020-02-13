package io.truereactive.demo.flow.main.home

import io.truereactive.library.flow.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

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

        val restoredState = channel.restoredState()
            .filter { it.containsKey(SAVED_INDEX_KEY) }
            .map {
                it.getInt(SAVED_INDEX_KEY)
            }//.share()

        launch {
            restoredState
                .map { index ->
                    FeedState(
                        sources = sources,
                        selectedPage = index,
                        restored = true
                    )
                }
                // .startWith(FeedState(sources))
                .onStart { emit(FeedState(sources)) }
                .distinctUntilChanged()
                .renderWhileAlive(channel)
        }

        launch {
            channel.viewEventsUntilDead {
                pageSelectedEvents
            }.saveState(channel) { bundle, data ->
                bundle.putInt(SAVED_INDEX_KEY, data)
            }
        }
    }

    companion object {
        private const val SAVED_INDEX_KEY = "pager_index_key"
    }

}