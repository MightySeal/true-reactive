package io.truereactive.demo.flickr.unused.details

import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.renderWhileAlive
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class DetailsPresenter(
    viewChannel: ViewChannel<DetailsViewEvents, String>,
    private val text: String,
    private val startIndex: Long
) : BasePresenter() {

    init {
        viewChannel.viewEvents.subscribe().untilDead()

        val interval = Observable.interval(1, TimeUnit.SECONDS)
            .map { it + startIndex }
            .share()

        val textTimer = interval
            .map { index -> "$text: $index" }
            .startWith(text)
            .observeOn(AndroidSchedulers.mainThread())

        textTimer
            // .bindActive(viewChannel)
            .renderWhileAlive(viewChannel)
            .untilDead()

        /*interval
            .saveState(viewChannel) { bundle, index -> bundle.putLong(TIME_KEY, index) }
            .untilDead()*/
    }

    companion object {
        const val TIME_KEY = "timer_key"
    }
}
