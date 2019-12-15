package io.truereactive.demo.flickr.common.data.repository

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.truereactive.demo.flickr.common.data.api.FlickrApi
import io.truereactive.demo.flickr.common.data.api.model.FlickrPhoto
import io.truereactive.demo.flickr.common.data.api.model.ImageSize
import io.truereactive.demo.flickr.common.data.domain.PhotoModel
import io.truereactive.demo.flickr.common.data.domain.PhotoSize
import io.truereactive.demo.flickr.common.data.domain.toDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotosRepository @Inject internal constructor(
    private val networkApi: FlickrApi
) {

    fun getRecent(): Single<List<PhotoModel>> = networkApi.getRecent()
        .map { it.photos.photoList.map(FlickrPhoto::toDomain) }
        .subscribeOn(Schedulers.io())

    fun search(text: String): Single<List<PhotoModel>> = networkApi.search(text)
        .map { it.photos.photoList.map(FlickrPhoto::toDomain) }
        .subscribeOn(Schedulers.io())

    fun getInfo(id: String): Single<PhotoModel> = networkApi.getInfo(id)
        .map { it.photo.toDomain() }
        .subscribeOn(Schedulers.io())

    fun getSizes(id: String): Single<List<PhotoSize>> = networkApi.getImageSizes(id)
        .map { it.sizes.sizes.map(ImageSize::toDomain) }
        .subscribeOn(Schedulers.io())


    // fun getImageSizes(id: String): Unit = networkApi.getImageSizes(id)
}