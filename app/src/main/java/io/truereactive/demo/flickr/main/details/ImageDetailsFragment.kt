package io.truereactive.demo.flickr.main.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import io.truereactive.core.abstraction.BaseFragment
import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.ViewEvents
import io.truereactive.demo.flickr.FlickrApplication
import io.truereactive.demo.flickr.R
import io.truereactive.demo.flickr.common.data.repository.PhotosRepository
import kotlinx.android.synthetic.main.fragment_flickr_image_details.*
import javax.inject.Inject

class ImageDetailsFragment : BaseFragment<ImageDetailsEvents, ImageDetails>() {

    @Inject
    lateinit var photosRepository: PhotosRepository

    private val glide by lazy(LazyThreadSafetyMode.NONE) {
        Glide.with(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_flickr_image_details, container, false)

    override fun render(model: ImageDetails) {
        glide
            .load(model.imageUrl)
            .into(image)
    }

    override fun createPresenter(
        viewChannel: ViewChannel<ImageDetailsEvents, ImageDetails>,
        args: Bundle?,
        savedState: Bundle?
    ): BasePresenter {
        (requireActivity().application as FlickrApplication).appComponent.imageDetailsComponent()
            .create()
            .inject(this)

        return ImageDetailsPresenter(
            viewChannel,
            photosRepository,
            args!!.getString(IMAGE_ID_KEY)!!
        )
    }

    override fun createViewHolder(view: View): ImageDetailsEvents {
        return ImageDetailsEvents(view)
    }

    companion object {

        private const val IMAGE_ID_KEY = "image_id"

        fun newInstance(imageId: String): ImageDetailsFragment = ImageDetailsFragment().apply {
            arguments = Bundle().also {
                it.putString(IMAGE_ID_KEY, imageId)
            }
        }
    }
}

class ImageDetailsEvents(view: View) : ViewEvents

data class ImageDetails(
    val imageUrl: String
)