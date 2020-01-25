package io.truereactive.demo.flickr.common.data.repository

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.truereactive.demo.flickr.common.data.api.flickr.FlickrApi
import io.truereactive.demo.flickr.common.data.api.flickr.model.FlickrPhoto
import io.truereactive.demo.flickr.common.data.api.flickr.model.ImageSize
import io.truereactive.demo.flickr.common.data.api.unsplash.UnsplashApi
import io.truereactive.demo.flickr.common.data.api.unsplash.model.UnsplashPhoto
import io.truereactive.demo.flickr.common.data.domain.PhotoModel
import io.truereactive.demo.flickr.common.data.domain.PhotoSize
import io.truereactive.demo.flickr.common.data.domain.Source
import io.truereactive.demo.flickr.common.data.domain.toDomain
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class PhotosRepository @Inject internal constructor(
    private val flickrApi: FlickrApi,
    private val unsplashApi: UnsplashApi
) {
    private val sourcesCount = Source.values().size

    fun getRecent(perPage: Int = 100): Observable<List<PhotoModel>> {
        val perSource = perPage / sourcesCount

        return Observable.combineLatest(
            flickrApi.getRecent(perSource)
                .map { it.photos.photoList.map(FlickrPhoto::toDomain) }
                .toObservable().startWith(emptyList<PhotoModel>())
                .subscribeOn(Schedulers.io()),
            unsplashApi.getPhotos(perSource)
                .map { it.map(UnsplashPhoto::toDomain) }
                .toObservable().startWith(emptyList<PhotoModel>())
                .subscribeOn(Schedulers.io()),
            BiFunction { fromFlickr: List<PhotoModel>, fromUnsplash: List<PhotoModel> ->
                merge(fromFlickr, fromUnsplash)
            }
        ).filter(List<PhotoModel>::isNotEmpty)
    }

    fun search(text: String): Observable<List<PhotoModel>> = Observable.combineLatest(
        flickrApi.search(text)
            .map { it.photos.photoList.map(FlickrPhoto::toDomain) }
            .toObservable().startWith(emptyList<PhotoModel>())
            .subscribeOn(Schedulers.io()),

        unsplashApi.search(text)
            .map { it.results.map(UnsplashPhoto::toDomain) }
            .toObservable().startWith(emptyList<PhotoModel>())
            .subscribeOn(Schedulers.io()),

        BiFunction { fromFlickr: List<PhotoModel>, fromUnsplash: List<PhotoModel> ->
            merge(fromFlickr, fromUnsplash)
        }
    )

    fun getInfo(id: String, source: Source): Single<PhotoModel> = when (source) {
        Source.Flickr -> {
            flickrApi.getInfo(id)
                .map { it.photo.toDomain() }
        }
        Source.Unsplash -> {
            unsplashApi.getPhoto(id)
                .map(UnsplashPhoto::toDomain)
        }
    }.subscribeOn(Schedulers.io())

    fun getSizes(id: String): Single<List<PhotoSize>> = flickrApi.getImageSizes(id)
        .map { it.sizes.sizes.map(ImageSize::toDomain) }
        .subscribeOn(Schedulers.io())

    private fun merge(
        fromFlickr: List<PhotoModel>,
        fromUnsplash: List<PhotoModel>
    ): List<PhotoModel> {
        val result = mutableListOf<PhotoModel>()

        repeat(max(fromFlickr.size, fromUnsplash.size)) { index ->
            fromFlickr.getOrNull(index)?.let(result::add)
            fromUnsplash.getOrNull(index)?.let(result::add)
        }

        return result.toList()
    }
}
