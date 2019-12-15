package io.truereactive.demo.flickr.main.home

import com.jakewharton.rxbinding3.appcompat.SearchViewQueryTextEvent
import io.reactivex.BackpressureStrategy
import io.reactivex.schedulers.Schedulers
import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.renderWhileAlive
import io.truereactive.core.reactiveui.saveState
import io.truereactive.core.reactiveui.viewEventsUntilDead
import io.truereactive.demo.flickr.common.data.device.NetworkStateRepository
import io.truereactive.demo.flickr.common.data.repository.PhotosRepository
import java.util.concurrent.TimeUnit

class SearchPresenter(
    private val channel: ViewChannel<SearchEvents, SearchState>,
    private val repository: PhotosRepository,
    private val networkState: NetworkStateRepository,
    private val initialState: String? = null
) : BasePresenter() {

    init {
        channel
            .viewEventsUntilDead { searchInput }
            .map(SearchViewQueryTextEvent::queryText)
            .throttleLast(200, TimeUnit.MILLISECONDS)
            .startWith(initialState ?: "")
            .distinctUntilChanged()
            .switchMapSingle { query ->
                if (query.isNotBlank()) {
                    repository.search(query.toString())
                } else {
                    repository.getRecent()
                }.retryWhen { errors ->
                    errors.flatMap { networkState.observable.toFlowable(BackpressureStrategy.LATEST) }
                        .filter { state -> state }
                        .observeOn(Schedulers.io())
                }
            }
            .map(::SearchState)
            .renderWhileAlive(channel)

        channel
            .viewEventsUntilDead { searchInput }
            .map { it.queryText }
            .saveState(channel) { bundle, text ->
                bundle.putString(INITIAL_STATE_KEY, text.toString())
            }
    }

    companion object {
        const val INITIAL_STATE_KEY = "restore_state_key"
    }
}