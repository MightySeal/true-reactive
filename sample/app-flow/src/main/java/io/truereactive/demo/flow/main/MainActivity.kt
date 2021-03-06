package io.truereactive.demo.flow.main

import android.os.Bundle
import android.view.View
import io.truereactive.demo.flow.R
import io.truereactive.demo.flow.main.home.FeedFragment
import io.truereactive.library.core.ViewEvents
import io.truereactive.library.flow.BaseActivity
import io.truereactive.library.flow.BasePresenter
import io.truereactive.library.flow.ViewChannel
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

class MainActivity : BaseActivity<MainFlickrEvents, Unit>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_flickr)

        Timber.i("========== onCreate activity")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("========== onDestroy activity")
    }

    override fun createPresenter(
        viewChannel: ViewChannel<MainFlickrEvents, Unit>,
        args: Bundle?,
        savedState: Bundle?,
        scope: CoroutineScope
    ): BasePresenter {
        return MainFlickrPresenter(viewChannel, scope)
    }

    override fun createViewHolder(view: View): MainFlickrEvents {
        return MainFlickrEvents(::openFeed)
    }

    override fun render(model: Unit) {}

    private fun openFeed() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, FeedFragment.newInstance(), null)
            .commit()
    }
}

class MainFlickrEvents(
    val openFeed: () -> Unit
) : ViewEvents