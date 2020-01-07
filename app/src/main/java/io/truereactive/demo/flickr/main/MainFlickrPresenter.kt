package io.truereactive.demo.flickr.main

import io.reactivex.android.schedulers.AndroidSchedulers
import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.mapUntilDead

class MainFlickrPresenter(
    viewChannel: ViewChannel<MainFlickrEvents, Unit>
) : BasePresenter() {

    init {
        viewChannel
            .mapUntilDead { openFeed }
            .firstElement()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { openPopular -> openPopular() }
            .untilDead()
    }
}