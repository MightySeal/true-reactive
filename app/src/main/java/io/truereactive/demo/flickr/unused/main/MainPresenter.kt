package io.truereactive.demo.flickr.unused.main

import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import timber.log.Timber
import javax.inject.Inject

class MainPresenter @Inject constructor(
    viewChannel: ViewChannel<MainViewEvents, Unit>
): BasePresenter() {

    init {
        viewChannel.viewEvents
            .map { it.openSearch }
            .firstElement()
            .subscribe {
                Timber.i("Open search")
                it()
            }.untilDead()
    }
}