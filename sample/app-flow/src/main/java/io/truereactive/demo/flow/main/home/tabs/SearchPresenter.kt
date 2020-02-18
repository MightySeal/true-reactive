package io.truereactive.demo.flow.main.home.tabs

import androidx.recyclerview.widget.DiffUtil
import com.jakewharton.rxbinding3.appcompat.SearchViewQueryTextEvent
import io.truereactive.demo.flickr.common.data.domain.PhotoModel
import io.truereactive.demo.flickr.common.data.repository.PhotosRepository
import io.truereactive.library.flow.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchPresenter(
    private val channel: ViewChannel<SearchEvents, SearchState>,
    private val searchEvents: Flow<SearchViewQueryTextEvent?>,
    private val repository: PhotosRepository,
    private val initialState: String? = null
) : BasePresenter() {

    init {

        // .retryWhen { errors -> // TODO: Unsplash has limits so need to add exponential backoff and better errors handling
        //    errors.flatMap { networkState.observable }
        //        .filter { state -> state }
        //        .observeOn(Schedulers.io())
        // }

        val restoredScrollPosition = channel.restoredState()
            .map {
                it.getInt(SCROLL_POSITION_KEY, -1)
            }.filter { it != -1 }
            .onStart { emit(-1) }

        val searchResults = searchEvents
            .map { it?.queryText }
            // .filter { it.isNotBlank() }
            // .throttleLast(300, TimeUnit.MILLISECONDS)
            // .startWith(initialState ?: "")
            .onStart { emit(initialState) }
            .distinctUntilChanged()
            .flatMapLatest { query ->
                query?.takeUnless(CharSequence::isBlank)?.let { repository.search(it.toString()) }
                    ?: repository.getRecent()
            }
        /*.switchMap { query ->
            if (query.isNotBlank()) {
                repository.search(query.toString())
            } else {
                repository.getRecent()
            }
        }.observeOn(Schedulers.computation())*/

        launch {
            combine(
                searchResults,
                restoredScrollPosition
            ) { search: List<PhotoModel>, scroll: Int ->
                Pair(search, scroll)
            }.scan(SearchState(emptyList(), null, null)) { prevState, newState ->
                val first = prevState.photos
                val second = newState.first
                val diff = diff(first, second)

                val scrollPosition = when {
                    prevState.scrollPosition != null -> null
                    newState.second == -1 -> null
                    else -> newState.second
                }
                SearchState(second, diff, scrollPosition)
            }.distinctUntilChanged { first, second -> first.photos == second.photos }
                .renderWhileAlive(channel)
        }

        /*Observable.combineLatest(
            searchResults,
            restoredScrollPosition,
            BiFunction { search: List<PhotoModel>, scroll: Int ->
                Pair(search, scroll)
            }).scan(SearchState(emptyList(), null, null), { prevState, newState ->

            val first = prevState.photos
            val second = newState.first
            val diff = diff(first, second)

            val scrollPosition = when {
                prevState.scrollPosition != null -> null
                newState.second == -1 -> null
                else -> newState.second
            }
            SearchState(second, diff, scrollPosition)
        }).distinctUntilChanged { first, second -> first.photos == second.photos }
            .renderWhileAlive(channel)*/

        launch {
            channel.viewEventsUntilDead {
                scrollState
            }.saveState(channel) { bundle, position ->
                bundle.putInt(SCROLL_POSITION_KEY, position)
            }
        }
    }

    /*private fun ref() {
        val restoredScrollPosition = channel.restoredState()
            .map {
                it.getInt(SCROLL_POSITION_KEY, -1)
            }.filter { it != -1 }
            .startWith(-1)

        // val restoredScrollPosition = Observable.just(-1)

        val searchResults = searchEvents
            .map(SearchViewQueryTextEvent::queryText)
            .filter(CharSequence::isNotBlank)
            .throttleLast(300, TimeUnit.MILLISECONDS)
            .startWith(initialState ?: "")
            .distinctUntilChanged()
            .switchMap { query ->
                if (query.isNotBlank()) {
                    repository.search(query.toString())
                } else {
                    repository.getRecent()
                }
            }.observeOn(Schedulers.computation())

        // TODO: Make library function for handling restored state
        Observable.combineLatest(
            searchResults,
            restoredScrollPosition,
            BiFunction { search: List<PhotoModel>, scroll: Int ->
                Pair(search, scroll)
            }).scan(SearchState(emptyList(), null, null), { prevState, newState ->

            val first = prevState.photos
            val second = newState.first
            val diff = diff(first, second)

            val scrollPosition = when {
                prevState.scrollPosition != null -> null
                newState.second == -1 -> null
                else -> newState.second
            }
            SearchState(second, diff, scrollPosition)
        }).distinctUntilChanged { first, second -> first.photos == second.photos }
            .renderWhileAlive(channel)

        channel.viewEventsUntilDead {
            scrollState
        }.saveState(channel) { bundle, position ->
            bundle.putInt(SCROLL_POSITION_KEY, position)
        }

    }*/

    private fun diff(first: List<PhotoModel>, second: List<PhotoModel>): DiffUtil.DiffResult =
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                first[oldItemPosition].id == second[oldItemPosition].id

            override fun getOldListSize(): Int = first.size

            override fun getNewListSize(): Int = second.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                first[oldItemPosition] == second[oldItemPosition]
        })

    companion object {
        const val INITIAL_STATE_KEY = "restore_state_key"
        private const val SCROLL_POSITION_KEY = "scroll_position"
    }
}