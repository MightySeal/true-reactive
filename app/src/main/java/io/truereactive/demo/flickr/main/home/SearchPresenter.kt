package io.truereactive.demo.flickr.main.home

import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.logLifecycle
import io.truereactive.core.reactiveui.renderWhileAlive
import io.truereactive.core.reactiveui.viewEventsUntilDead
import io.truereactive.demo.flickr.data.repository.PhotosRepository
import java.util.concurrent.TimeUnit

class SearchPresenter(
    private val channel: ViewChannel<SearchEvents, SearchState>,
    private val repository: PhotosRepository
) : BasePresenter() {

    init {
        channel
            .viewEventsUntilDead { searchInput.logLifecycle("========= Lifecycle =========") }
            .throttleLast(200, TimeUnit.MILLISECONDS)
            .startWith("")
            .distinctUntilChanged()
            .switchMapSingle {
                if (it.isNotBlank()) {
                    repository.search(it)
                } else {
                    repository.getRecent()
                }
            }
            .map(::SearchState)
            .renderWhileAlive(channel)
        // .untilDead() <------- TODO: fixes the problem but why?
    }

}