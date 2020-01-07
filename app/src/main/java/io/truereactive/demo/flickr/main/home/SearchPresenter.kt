package io.truereactive.demo.flickr.main.home

import com.jakewharton.rxbinding3.appcompat.SearchViewQueryTextEvent
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.renderWhileAlive
import io.truereactive.demo.flickr.common.data.device.NetworkStateRepository
import io.truereactive.demo.flickr.common.data.repository.PhotosRepository
import java.util.concurrent.TimeUnit

class SearchPresenter(
    private val channel: ViewChannel<SearchEvents, SearchState>,
    private val searchEvents: Observable<SearchViewQueryTextEvent>,
    private val repository: PhotosRepository,
    private val networkState: NetworkStateRepository,
    private val initialState: String? = null
) : BasePresenter() {

    init {
        searchEvents
            .map(SearchViewQueryTextEvent::queryText)
            .filter(CharSequence::isNotBlank)
            .throttleLast(300, TimeUnit.MILLISECONDS)
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
    }

    companion object {
        const val INITIAL_STATE_KEY = "restore_state_key"
    }
}