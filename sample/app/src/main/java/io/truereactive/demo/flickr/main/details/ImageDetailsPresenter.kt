package io.truereactive.demo.flickr.main.details

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.truereactive.demo.flickr.common.data.domain.Source
import io.truereactive.demo.flickr.common.data.repository.PhotosRepository
import io.truereactive.library.rx.abstraction.BasePresenter
import io.truereactive.library.rx.abstraction.ViewChannel
import io.truereactive.library.rx.reactiveui.renderWhileAlive

class ImageDetailsPresenter constructor(
    viewChannel: ViewChannel<ImageDetailsEvents, ImageDetails>,
    private val repository: PhotosRepository,
    private val imageId: String,
    private val imageUrl: String,
    private val imageSource: Source
) : BasePresenter() {

    init {

        val imageObservable = when (imageSource) {
            Source.Flickr -> Observable.combineLatest(
                repository.getInfo(imageId, imageSource)
                    .map<Pair<String?, String?>> { it.title to it.owner }
                    .toObservable()
                    .startWith(Pair<String?, String?>(null, null)),
                repository.getSizes(imageId)
                    .filter { sizes -> sizes.isNotEmpty() }
                    .map { sizes -> sizes.maxBy { size -> size.imageWidth }?.staticUrl!! }
                    .toObservable()
                    .startWith(imageUrl),
                BiFunction { description: Pair<String?, String?>, imageUrl: String ->
                    ImageDetails(imageUrl, description.first, description.second)
                }
            )

            // TODO: remodel domain
            Source.Unsplash -> repository.getInfo(imageId, imageSource)
                .map {
                    ImageDetails(it.square, null, it.owner)
                }
                .toObservable()
        }

        imageObservable
            .renderWhileAlive(viewChannel)
    }
}