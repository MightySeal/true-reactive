package io.truereactive.demo.flow.main.home

import io.truereactive.library.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class FeedPresenter(
    private val channel: ViewChannel<FeedViewEvents, FeedState>,
    scope: CoroutineScope
) : BasePresenter(scope) {

    init {
        // TODO: make different sources instead of different queries (i.e. Flickr, Unsplash, Dribbble, etc)
        val sources = listOf(
            "London",
            "Zurich",
            "Copenhagen",
            "Paris",
            "Amsterdam"
        )/*.let {
            emptyList<String>()
        }*/

        val restoredState = channel.restoredState()
            .filter { it.containsKey(SAVED_INDEX_KEY) }
            .map {
                it.getInt(SAVED_INDEX_KEY)
            }.onEach {
                Timber.i("Kekekeke $it")
            }

        //.share()

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

        launch(Dispatchers.Main.immediate) {
            channel.viewEventsUntilDead {
                pageSelectedEvents
            }.saveState(channel) { bundle, data ->
                bundle.putInt(SAVED_INDEX_KEY, data)
            }
        }

        launch {
            Timber.i("++++++++++ Collect input")
            channel.viewEventsUntilDead("Input") {
                searchInput
            }.onStart { Timber.i("++++++++++ input start") }
                .onCompletion { Timber.i("++++++++++ input completion") }
                .collect {
                    Timber.i("++++++++++ Input ${it.queryText}")
                }
        }
    }

    companion object {
        private const val SAVED_INDEX_KEY = "pager_index_key"
    }

}