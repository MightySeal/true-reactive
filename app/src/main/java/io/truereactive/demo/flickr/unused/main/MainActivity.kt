package io.truereactive.demo.flickr.unused.main

import android.os.Bundle
import android.view.View
import io.truereactive.core.abstraction.BaseActivity
import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.demo.flickr.R
import io.truereactive.demo.flickr.unused.details.DetailsFragment
import io.truereactive.demo.flickr.unused.search.SearchFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

@ExperimentalCoroutinesApi
class MainActivity : BaseActivity<MainViewEvents, Unit>() {

    override fun createPresenter(
        viewChannel: ViewChannel<MainViewEvents, Unit>,
        args: Bundle?,
        savedState: Bundle?
    ): BasePresenter {
        return MainPresenter(viewChannel)
    }

    override fun createViewHolder(view: View): MainViewEvents {
        return MainViewEvents(view, ::openSearch)
    }

    override fun render(model: Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStop() {
        super.onStop()
        Timber.i("Activity stopped")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("Activity destroyed")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        /*Handler().postDelayed({
            startActivity(Intent(this, SecondActivity::class.java))
        }, 5_000)*/

        /*Handler().postDelayed({
            supportFragmentManager
                .beginTransaction()
                // .add(R.id.content, DetailsFragment(), null)
                .replace(R.id.content, DetailsFragment(), null)
                .addToBackStack(null)
                .commit()
        }, 5_000)*/
    }

    private fun openSearch() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content, SearchFragment(), null)
            .commit()
    }

    fun openDetails(text: String) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content, DetailsFragment.newInstance(text), null)
            .addToBackStack(null)
            .commit()
    }
}
