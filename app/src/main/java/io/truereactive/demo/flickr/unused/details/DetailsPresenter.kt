package io.truereactive.demo.flickr.unused.details

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.renderWhileAlive
import java.util.concurrent.TimeUnit

class DetailsPresenter(
    viewChannel: ViewChannel<DetailsViewEvents, String>,
    private val text: String,
    private val startIndex: Long
) : BasePresenter() {

    init {
        val interval = Observable.interval(1, TimeUnit.SECONDS)
            .map { it + startIndex }
            .share()

        val textTimer = interval
            .map { index -> "$text: $index" }
            .startWith(text)
            .observeOn(AndroidSchedulers.mainThread())

        textTimer
            .renderWhileAlive(viewChannel)
            .untilDead()
    }

    companion object {
        const val TIME_KEY = "timer_key"
    }
}
