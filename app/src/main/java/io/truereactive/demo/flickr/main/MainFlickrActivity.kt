package io.truereactive.demo.flickr.main

import android.os.Bundle
import android.view.View
import io.truereactive.core.abstraction.BaseActivity
import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.ViewEvents
import io.truereactive.demo.flickr.R
import io.truereactive.demo.flickr.common.data.domain.PhotoModel
import io.truereactive.demo.flickr.main.details.ImageDetailsFragment
import io.truereactive.demo.flickr.main.home.FeedFragment
import io.truereactive.demo.flickr.main.home.tabs.SearchFragment

class MainFlickrActivity : BaseActivity<MainFlickrEvents, Unit>() {

    override fun render(model: Unit) {}

    override fun createPresenter(
        viewChannel: ViewChannel<MainFlickrEvents, Unit>,
        args: Bundle?,
        savedState: Bundle?
    ): BasePresenter =
        MainFlickrPresenter(viewChannel)

    override fun createViewHolder(view: View): MainFlickrEvents {
        return MainFlickrEvents(::openFeed)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_flickr)
    }

    override fun onDestroy() {
        super.onDestroy()

        val fm = supportFragmentManager
    }

    fun openDetails(photoModel: PhotoModel) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, ImageDetailsFragment.newInstance(photoModel), null)
            .addToBackStack(null)
            .commit()
    }

    private fun openPopular() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, SearchFragment.newInstance(), null)
            .commit()
    }

    private fun openFeed() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, FeedFragment.newInstance(), null)
            .commit()
    }
}

class MainFlickrEvents(
    // val openPopular: () -> Unit
    val openFeed: () -> Unit
) : ViewEvents