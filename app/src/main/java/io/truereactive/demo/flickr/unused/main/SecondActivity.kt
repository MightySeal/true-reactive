package io.truereactive.demo.flickr.unused.main

import android.os.Bundle
import android.view.View
import io.truereactive.core.abstraction.BaseActivity
import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.ViewEvents

class SecondActivity : BaseActivity<SecondActivityEvents, Unit>() {
    override fun createPresenter(
        viewChannel: ViewChannel<SecondActivityEvents, Unit>,
        args: Bundle?,
        savedState: Bundle?
    ): BasePresenter {
        return SecondPresenter(viewChannel)
    }

    override fun createViewHolder(view: View): SecondActivityEvents {
        return SecondActivityEvents()
    }

    override fun render(model: Unit) {}
}

class SecondActivityEvents(): ViewEvents

class SecondPresenter(viewChannel: ViewChannel<SecondActivityEvents, Unit>) : BasePresenter()