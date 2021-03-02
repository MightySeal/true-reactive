package io.truereactive.demo.flow.main

import io.truereactive.library.flow.BasePresenter
import io.truereactive.library.flow.ViewChannel
import io.truereactive.library.flow.mapUntilDead
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

class MainFlickrPresenter(
    viewChannel: ViewChannel<MainFlickrEvents, Unit>,
    scope: CoroutineScope
) : BasePresenter(scope) {

    init {
        launch {

            Timber.i("========== launch job")

            viewChannel
                .mapUntilDead { openFeed }
                .take(1)
                .collect { openPopular ->
                    openPopular()
                }

            /*viewChannel
                .mapUntilDead { openFeed }
                .map { { Timber.i("Invoke navigation") } }
                .onEach { Timber.i("========== map open feed") }
                .onCompletion { Timber.i("========== Complete mapping") }
                .first().invoke()*/

            /*viewChannel
                .mapUntilDead { openFeed }
                .onEach { Timber.i("========== map open feed") }
                .onCompletion { Timber.i("========== Complete mapping") }
                .collect {
                    openPopular -> openPopular()
                }*/
        }
    }
}