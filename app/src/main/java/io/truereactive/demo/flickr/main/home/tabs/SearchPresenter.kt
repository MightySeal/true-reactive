package io.truereactive.demo.flickr.main.home.tabs

import androidx.recyclerview.widget.DiffUtil
import com.jakewharton.rxbinding3.appcompat.SearchViewQueryTextEvent
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.renderWhileAlive
import io.truereactive.demo.flickr.common.data.device.NetworkStateRepository
import io.truereactive.demo.flickr.common.data.domain.PhotoModel
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
            .switchMap { query ->
                if (query.isNotBlank()) {
                    repository.search(query.toString())
                } else {
                    repository.getRecent()
                }/*.retryWhen { errors -> // TODO: Unsplash has limits so need to add exponential backoff and better errors handling
                    errors.flatMap { networkState.observable }
                        .filter { state -> state }
                        .observeOn(Schedulers.io())
                }*/
            }.observeOn(Schedulers.computation())
            .scan(Pair(emptyList<PhotoModel>(), emptyList<PhotoModel>()), { first, second ->
                Pair(first.second, second)
            })
            .map { (first, second) ->
                val diff = diff(first, second)
                SearchState(second, diff)
            }
            .distinctUntilChanged { first, second -> first.photos == second.photos }
            .renderWhileAlive(channel)
    }

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
    }
}