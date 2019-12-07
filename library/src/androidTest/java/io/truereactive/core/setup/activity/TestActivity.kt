package io.truereactive.core.setup.activity

import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.jakewharton.rxbinding3.view.clicks
import io.truereactive.core.abstraction.BaseActivity
import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.ViewEvents
import io.truereactive.core.reactiveui.renderWhileActive
import io.truereactive.core.reactiveui.viewEventsUntilDead
import io.truereactive.library.test.R

class TestActivity : BaseActivity<TestActivityEvents, String>() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }

    override fun render(model: String) {

    }

    override fun createPresenter(
        viewChannel: ViewChannel<TestActivityEvents, String>,
        args: Bundle?,
        savedState: Bundle?
    ): BasePresenter {
        return TestPresenter(viewChannel)
    }

    override fun createViewHolder(view: View): TestActivityEvents {
        return TestActivityEvents(view)
    }
}

class TestPresenter(
    viewChannel: ViewChannel<TestActivityEvents, String>
) : BasePresenter() {
    init {
        viewChannel
            .viewEventsUntilDead { clicks }
            .scan(1, { counter, emit -> counter.inc() })
            .map(Int::toString)
            .renderWhileActive(viewChannel)
    }
}


class TestActivityEvents(
    view: View
) : ViewEvents {
    val clicks = view.findViewById<EditText>(R.id.clickButton).clicks()
}