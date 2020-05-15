package io.truereactive.demo.flow.main.home

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
import io.truereactive.demo.flow.FlowApplication
import io.truereactive.demo.flow.R
import io.truereactive.demo.flow.main.home.di.HomeComponent
import io.truereactive.library.core.ViewEvents
import io.truereactive.library.flow.*
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.fragment_feed.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
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

    // TODO: Fix buggy viewpager
    //  https://www.reddit.com/r/androiddev/comments/co3ts6/reusing_viewpager2_fragmentstateadapter_in/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sourcesPager.offscreenPageLimit = 1
        bar.replaceMenu(R.menu.home_menu)

        // sourcesPager.isSaveEnabled = false


        adapter = createAdapter(null)
        /*if (!::adapter.isInitialized) {
            adapter = createAdapter(null)
        } else {
            sourcesPager.adapter = adapter
        }*/
    }


    // Dirty hack to invalidate ViewPager state so the adapter can be re-attached
    override fun onDestroyView() {
        super.onDestroyView()
        sourcesPager.adapter = null
    }

    override fun onResume() {
        super.onResume()
    }

    override fun render(model: FeedState) {
        Timber.i("========== Render $model")
        adapter.setSources(model.sources)
        model.selectedPage?.let {
            sourcesPager.setCurrentItem(it, false)
        }
    }

    /*override fun render(model: FeedState) {
        if (model.restored) {
            adapter = createAdapter(model.sources)
        } else {
            if (!::adapter.isInitialized) {
                Timber.i("========== Re-create adapter")
                adapter = createAdapter(null)
            }

            Timber.i("========== Set sources")
            adapter.setSources(model.sources)
        }
        model.selectedPage?.let {
            sourcesPager.setCurrentItem(it, false)
        }
    }*/

    override fun createPresenter(
        viewChannel: ViewChannel<FeedViewEvents, FeedState>,
        args: Bundle?,
        savedState: Bundle?,
        scope: CoroutineScope
    ): BasePresenter {
        val searchEvents = viewChannel.viewEventsUntilDead { searchInput }

        val component =
            (requireActivity().application as FlowApplication).appComponent.homeComponent()
                .create(searchEvents)
        component.inject(this)
        putCache(component)

        return FeedPresenter(
            viewChannel,
            scope
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

        // sourcesTabs.tabIndicator

        return adapter
    }

    companion object {
        fun newInstance(): FeedFragment =
            FeedFragment()
    }
}


class FeedViewEvents(view: View, searchView: SearchView) :
    ViewEvents {
    /*val pageSelectedEvents: Observable<Int> =
        (view.sourcesPager?.events() ?: Observable.empty()).share()*/

    val pageSelectedEvents: Flow<Int> = view.sourcesPager.events().share().asFlow()

    val searchInput: Flow<SearchViewQueryTextEvent> = searchView.queryTextChangeEvents()
        .skipInitialValue()
        .doOnSubscribe { Timber.i("++++++++++ Input subscribe") }
        .doOnComplete { Timber.i("++++++++++ Input complete") }
        .doOnDispose { Timber.i("++++++++++ Input dispose") }
        .doOnError { Timber.i("++++++++++ Input error") }
        .throttleLast(300, TimeUnit.MILLISECONDS)
        .share()
        .asFlow("Input")
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