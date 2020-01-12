package io.truereactive.demo.flickr.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.jakewharton.rxbinding3.appcompat.SearchViewQueryTextEvent
import com.jakewharton.rxbinding3.appcompat.queryTextChangeEvents
import io.reactivex.Observable
import io.truereactive.core.abstraction.*
import io.truereactive.core.reactiveui.ViewEvents
import io.truereactive.core.reactiveui.viewEventsUntilDead
import io.truereactive.demo.flickr.FlickrApplication
import io.truereactive.demo.flickr.R
import io.truereactive.demo.flickr.main.home.di.HomeComponent
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.fragment_feed.view.*
import java.util.concurrent.TimeUnit

class FeedFragment : BaseFragment<FeedViewEvents, FeedState>() {

    private lateinit var adapter: FeedSourcesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sourcesPager.offscreenPageLimit = 1
        bar.replaceMenu(R.menu.home_menu)
    }

    override fun render(model: FeedState) {
        if (model.restored) {
            adapter = createAdapter(model.sources)
        } else {
            if (!::adapter.isInitialized) {
                adapter = createAdapter(null)
            }

            adapter.setSources(model.sources)
        }
        model.selectedPage?.let {
            sourcesPager.setCurrentItem(it, false)
        }
    }

    override fun createPresenter(
        viewChannel: ViewChannel<FeedViewEvents, FeedState>,
        args: Bundle?,
        savedState: Bundle?
    ): BasePresenter {
        val searchEvents = viewChannel.viewEventsUntilDead { searchInput }.share()

        val component =
            (requireActivity().application as FlickrApplication).appComponent.homeComponent()
                .create(searchEvents)
        component.inject(this)
        putCache(component)

        return FeedPresenter(
            viewChannel
        )
    }

    override fun createViewHolder(view: View): FeedViewEvents {
        val searchItem = bar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        return FeedViewEvents(
            view,
            searchView
        )
    }

    fun getComponent(): HomeComponent = get() as HomeComponent

    private fun createAdapter(sources: List<String>?): FeedSourcesAdapter {
        val adapter = sources?.let {
            FeedSourcesAdapter(this, it)
        } ?: FeedSourcesAdapter(this)

        sourcesPager.adapter = adapter

        TabLayoutMediator(sourcesTabs, sourcesPager) { tab, position ->
            tab.text = adapter.getTabTitle(position)
        }.attach()

        return adapter
    }

    companion object {
        fun newInstance(): FeedFragment =
            FeedFragment()
    }
}


class FeedViewEvents(view: View, searchView: SearchView) : ViewEvents {
    val pageSelectedEvents: Observable<Int> =
        (view.sourcesPager?.events() ?: Observable.empty()).share()

    val searchInput: Observable<SearchViewQueryTextEvent> = searchView.queryTextChangeEvents()
        .skipInitialValue()
        .throttleLast(300, TimeUnit.MILLISECONDS)
        .share()
}

data class FeedState(
    val sources: List<String>,
    val selectedPage: Int? = null,
    val restored: Boolean = false
)

fun ViewPager2.events() = Observable.create<Int> { emitter ->

    val listener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if (!emitter.isDisposed) {
                emitter.onNext(position)
            }
        }
    }

    emitter.setCancellable {
        unregisterOnPageChangeCallback(listener)
    }

    registerOnPageChangeCallback(listener)
}