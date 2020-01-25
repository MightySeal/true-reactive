package io.truereactive.demo.flickr.main.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import io.truereactive.demo.flickr.FlickrApplication
import io.truereactive.demo.flickr.R
import io.truereactive.demo.flickr.common.data.domain.PhotoModel
import io.truereactive.demo.flickr.common.data.domain.Source
import io.truereactive.demo.flickr.common.data.repository.PhotosRepository
import io.truereactive.library.core.ViewEvents
import io.truereactive.library.rx.abstraction.BaseFragment
import io.truereactive.library.rx.abstraction.BasePresenter
import io.truereactive.library.rx.abstraction.ViewChannel
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
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(image)

        title.text = model.imageTitle
        owner.text = model.owner
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
            args!!.getString(IMAGE_ID_KEY)!!,
            args.getString(IMAGE_URL_KEY)!!,
            Source.valueOf(args.getString(IMAGE_SOURCE_KEY)!!)
        )
    }

    override fun createViewHolder(view: View): ImageDetailsEvents {
        return ImageDetailsEvents(view)
    }

    companion object {

        private const val IMAGE_ID_KEY = "image_id"
        private const val IMAGE_URL_KEY = "image_url"
        private const val IMAGE_SOURCE_KEY = "image_source"

        fun newInstance(photoModel: PhotoModel): ImageDetailsFragment =
            ImageDetailsFragment().apply {
                arguments = Bundle().also {
                    it.putString(IMAGE_ID_KEY, photoModel.id)
                    it.putString(IMAGE_URL_KEY, photoModel.previewSquare)
                    it.putString(IMAGE_SOURCE_KEY, photoModel.source.name)
                }
            }
    }
}

class ImageDetailsEvents(view: View) : ViewEvents

data class ImageDetails(
    val imageUrl: String,
    val imageTitle: String?,
    val owner: String?
)