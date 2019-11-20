package io.truereactive.demo.flickr.main.home

import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.logEmissions
import io.truereactive.core.reactiveui.renderWhileAlive
import io.truereactive.core.reactiveui.viewEventsUntilDead
import io.truereactive.demo.flickr.data.repository.PhotosRepository

class SearchPresenter(
    private val channel: ViewChannel<SearchEvents, SearchState>,
    private val repository: PhotosRepository
) : BasePresenter() {

    init {
        channel
            .viewEventsUntilDead { searchInput }
            .distinctUntilChanged()
            .switchMapSingle {
                if (it.isNotBlank()) {
                    repository.search(it)
                } else {
                    repository.getRecent()
                }
            }
            .startWith(repository.getRecent().toObservable())
            .map(::SearchState)
            .renderWhileAlive(channel)
    }

}