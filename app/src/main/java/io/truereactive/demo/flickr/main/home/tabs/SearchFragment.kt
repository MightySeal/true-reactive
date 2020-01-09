package io.truereactive.demo.flickr.main.home.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding3.appcompat.SearchViewQueryTextEvent
import io.reactivex.Observable
import io.truereactive.core.abstraction.BaseFragment
import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.ViewEvents
import io.truereactive.demo.flickr.R
import io.truereactive.demo.flickr.common.data.device.NetworkStateRepository
import io.truereactive.demo.flickr.common.data.domain.PhotoModel
import io.truereactive.demo.flickr.common.data.repository.PhotosRepository
import io.truereactive.demo.flickr.main.MainFlickrActivity
import io.truereactive.demo.flickr.main.home.FeedFragment
import kotlinx.android.synthetic.main.fragment_flickr_search.*
import javax.inject.Inject

class SearchFragment : BaseFragment<SearchEvents, SearchState>() {

    @Inject
    lateinit var photosRepository: PhotosRepository
    @Inject
    lateinit var networkStateRepository: NetworkStateRepository
    @Inject
    lateinit var searchEvents: Observable<SearchViewQueryTextEvent>

    private val photosAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val requestManager = Glide.with(this)
        PhotosAdapter(
            requireActivity(),
            requestManager
        ) {
            (requireActivity() as MainFlickrActivity).openDetails(it)
        }
    }

    override fun render(model: SearchState) {
        photosAdapter.replace(model.photos)
    }

    override fun createPresenter(
        viewChannel: ViewChannel<SearchEvents, SearchState>,
        args: Bundle?,
        savedState: Bundle?
    ): BasePresenter {

        val component = (requireParentFragment() as FeedFragment).getComponent()
        component
            .searchComponent()
            .create()
            .inject(this)

        return SearchPresenter(
            viewChannel,
            searchEvents,
            photosRepository,
            networkStateRepository,
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
}

data class SearchState(
    val photos: List<PhotoModel>
)