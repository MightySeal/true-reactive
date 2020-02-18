package io.truereactive.demo.flow.main

import io.truereactive.library.flow.BasePresenter
import io.truereactive.library.flow.ViewChannel
import io.truereactive.library.flow.mapUntilDead
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainFlickrPresenter(
    viewChannel: ViewChannel<MainFlickrEvents, Unit>
) : BasePresenter() {

    init {
        launch {

            viewChannel
                .mapUntilDead { openFeed }
                // .first()
                // .observeOn(AndroidSchedulers.mainThread())
                .collect { openPopular -> openPopular() }
            // .untilDead()
        }
    }
}