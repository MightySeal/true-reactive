package io.truereactive.demo.flow.main.home.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding3.appcompat.SearchViewQueryTextEvent
import io.reactivex.Observable
import io.truereactive.demo.core.RecyclerData
import io.truereactive.demo.flickr.common.data.domain.PhotoModel
import io.truereactive.demo.flickr.common.data.repository.PhotosRepository
import io.truereactive.demo.flow.R
import io.truereactive.demo.flow.main.home.FeedFragment
import io.truereactive.library.core.ViewEvents
import io.truereactive.library.flow.BaseFragment
import io.truereactive.library.flow.BasePresenter
import io.truereactive.library.flow.ViewChannel
import io.truereactive.library.flow.asFlow
import kotlinx.android.synthetic.main.fragment_flickr_search.*
import kotlinx.android.synthetic.main.fragment_flickr_search.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchFragment : BaseFragment<SearchEvents, SearchState>() {


    @Inject
    lateinit var photosRepository: PhotosRepository
    @Inject
    lateinit var searchEvents: Flow<SearchViewQueryTextEvent>

    private val photosAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val requestManager = Glide.with(this)
        PhotosAdapter(
            requireActivity(),
            requestManager
        ) {
            // (requireActivity() as MainActivity).openDetails(it)
        }
    }

    override fun render(model: SearchState) {
        photosAdapter.replace(RecyclerData(model.photos, model.photosDiff))

        model.scrollPosition?.let(photosList::scrollToPosition)
    }

    override fun createPresenter(
        viewChannel: ViewChannel<SearchEvents, SearchState>,
        args: Bundle?,
        savedState: Bundle?,
        scope: CoroutineScope
    ): BasePresenter {

        val component = (requireParentFragment() as FeedFragment).getComponent()
        component
            .searchComponent()
            .create()
            .inject(this)

        return SearchPresenter(
            scope,
            viewChannel,
            searchEvents,
            photosRepository,
            savedState?.getString(SearchPresenter.INITIAL_STATE_KEY)
                ?: args?.getString(SEARCH_KEY)
        )
    }

    override fun createViewHolder(view: View): SearchEvents {
        return SearchEvents(view)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_flickr_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photosList.layoutManager = GridLayoutManager(requireContext(), 2)
        photosList.adapter = photosAdapter
    }

    /*override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val visibleItem = (photosList.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        outState.putInt(SCROLL_POSITION_KEY, visibleItem)
    }*/

    companion object {
        private const val SEARCH_KEY = "search_key"

        fun newInstance(search: String? = null): SearchFragment = SearchFragment().apply {
            if (search != null) {
                arguments = Bundle().also {
                    it.putString(SEARCH_KEY, search)
                }
            }
        }
    }
}

class SearchEvents(view: View) : ViewEvents {
    val scrollState = view.photosList.observeFirstVisiblePosition().share().asFlow()
}

data class SearchState(
    val photos: List<PhotoModel>,
    val photosDiff: DiffUtil.DiffResult?,
    val scrollPosition: Int?
) {
    override fun toString(): String {
        return "${photos.size} $photosDiff"
    }
}

private fun RecyclerView.observeFirstVisiblePosition(): Observable<Int> =
    Observable.create { emitter ->

        val lm = layoutManager as? LinearLayoutManager

        if (lm == null) {
            layoutManager.let { manager ->
                if (manager == null) {
                    emitter.onError(Exception("LayoutManager is not set"))
                } else {
                    emitter.onError(Exception("Expected LinearLayoutManager, got ${manager::class.simpleName} instead"))
                }
            }
        } else {
            val listener = object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                    if (!emitter.isDisposed) {
                        emitter.onNext(lm.findFirstVisibleItemPosition())
                    }
                }
            }

            emitter.setCancellable {
                this.removeOnScrollListener(listener)
            }

            this.addOnScrollListener(listener)

            emitter.onNext(lm.findFirstVisibleItemPosition())
        }
    }