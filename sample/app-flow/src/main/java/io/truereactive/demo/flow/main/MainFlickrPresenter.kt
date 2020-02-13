package io.truereactive.demo.flow.main

import io.truereactive.library.flow.BasePresenter
import io.truereactive.library.flow.ViewChannel
import io.truereactive.library.flow.mapUntilDead
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

class MainFlickrPresenter(
    viewChannel: ViewChannel<MainFlickrEvents, Unit>
) : BasePresenter() {

    init {
        Timber.i("========== Create presenter")
        launch {

            Timber.i("========== Launch in init")
            viewChannel
                .mapUntilDead { openFeed }
                .onEach {
                    Timber.i("========== Emit open feed")
                }
                .onStart {
                    Timber.i("========== Launch start")
                }
                // .first()
                // .observeOn(AndroidSchedulers.mainThread())
                .collect { openPopular -> openPopular() }
            // .untilDead()
        }
    }
}