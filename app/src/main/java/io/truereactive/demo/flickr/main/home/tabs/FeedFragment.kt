package io.truereactive.demo.flickr.main.home.tabs

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

class FeedFragment : BaseFragment<FeedViewEvents, FeedState>() {

    private val adapter by lazy(LazyThreadSafetyMode.NONE) {
        FeedSourcesAdapter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sourcesPager.offscreenPageLimit = 2
        sourcesPager.adapter = adapter

        bar.replaceMenu(R.menu.home_menu)

        TabLayoutMediator(sourcesTabs, sourcesPager) { tab, position ->
            tab.text = adapter.getTabTitle(position)
        }.attach()
    }

    override fun render(model: FeedState) {
        adapter.setSources(model.sources)
    }

    override fun createPresenter(
        viewChannel: ViewChannel<FeedViewEvents, FeedState>,
        args: Bundle?,
        savedState: Bundle?
    ): BasePresenter {
        val searchEvents = viewChannel.viewEventsUntilDead { searchInput }

        val component =
            (requireActivity().application as FlickrApplication).appComponent.homeComponent()
                .create(searchEvents)
        component.inject(this)
        putCache(component)

        return FeedPresenter(viewChannel)
    }

    override fun createViewHolder(view: View): FeedViewEvents {
        val searchItem = bar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        return FeedViewEvents(view, searchView)
    }

    fun getComponent(): HomeComponent = get() as HomeComponent

    companion object {
        fun newInstance(): FeedFragment = FeedFragment()
    }
}


class FeedViewEvents(view: View, searchView: SearchView) : ViewEvents {
    val pageSelectedEvents: Observable<Int> = view.sourcesPager?.events() ?: Observable.empty()

    val searchInput: Observable<SearchViewQueryTextEvent> = searchView.queryTextChangeEvents()
        .skipInitialValue()
        .share()
}

data class FeedState(
    val sources: List<String>
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