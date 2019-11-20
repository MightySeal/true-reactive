package io.truereactive.demo.flickr.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.truereactive.core.abstraction.BaseFragment
import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.ViewEvents
import io.truereactive.core.reactiveui.logLifecycle
import io.truereactive.core.viewbinding.input
import io.truereactive.demo.flickr.FlickrApplication
import io.truereactive.demo.flickr.R
import io.truereactive.demo.flickr.data.domain.PhotoModel
import io.truereactive.demo.flickr.data.repository.PhotosRepository
import kotlinx.android.synthetic.main.fragment_flickr_search.*
import kotlinx.android.synthetic.main.fragment_flickr_search.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx2.asObservable
import timber.log.Timber
import javax.inject.Inject

class SearchFragment : BaseFragment<SearchEvents, SearchState>() {

    @Inject
    lateinit var photosRepository: PhotosRepository

    private val photosAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val requestManager = Glide.with(this)
        PhotosAdapter(requireActivity(), requestManager)
    }

    override fun render(model: SearchState) {
        photosAdapter.replace(model.photos)
    }

    override fun createPresenter(
        viewChannel: ViewChannel<SearchEvents, SearchState>,
        args: Bundle?,
        savedState: Bundle?
    ): BasePresenter {
        (requireActivity().application as FlickrApplication).appComponent.searchComponent().create().inject(this)
        return SearchPresenter(viewChannel, photosRepository)
    }

    override fun createViewHolder(view: View): SearchEvents = SearchEvents(view)

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
        fun newInstance(): SearchFragment = SearchFragment()
    }
}

class SearchEvents(view: View) : ViewEvents {
    val searchInput: Observable<String> = view.search.input()
}

data class SearchState(
    val photos: List<PhotoModel>
)