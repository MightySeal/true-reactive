package io.truereactive.demo.flickr.unused.main

import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.mapUntilDead
import javax.inject.Inject

class MainPresenter @Inject constructor(
    viewChannel: ViewChannel<MainViewEvents, Unit>
) : BasePresenter() {

    init {
        viewChannel.mapUntilDead {
            openSearch
        }.firstElement()
            .subscribe {
                it()
            }.untilDead()
    }
}