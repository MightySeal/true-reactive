package io.truereactive.demo.flickr.main.home.tabs

import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.renderWhileAlive

class FeedPresenter(
    private val channel: ViewChannel<FeedViewEvents, FeedState>
) : BasePresenter() {

    init {
        // TODO: make different sources instead of different queries (i.e. Flickr, Unsplash, Dribbble, etc)
        FeedState(listOf("London", "Zurich", "Copenhagen", "Paris", "Amsterdam"))
            .renderWhileAlive(channel)
    }

}