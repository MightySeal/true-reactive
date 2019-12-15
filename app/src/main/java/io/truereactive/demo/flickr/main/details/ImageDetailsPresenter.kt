package io.truereactive.demo.flickr.main.details

import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.core.reactiveui.renderWhileAlive
import io.truereactive.demo.flickr.common.data.repository.PhotosRepository

class ImageDetailsPresenter constructor(
    viewChannel: ViewChannel<ImageDetailsEvents, ImageDetails>,
    private val repository: PhotosRepository,
    private val imageId: String
) : BasePresenter() {

    init {
        repository.getInfo(imageId)
            .map { photo -> ImageDetails(photo.previewSquare) }
            .toObservable()
            .renderWhileAlive(viewChannel)
    }
}