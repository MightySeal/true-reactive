package io.truereactive.demo.flickr.data.repository

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.truereactive.demo.flickr.data.api.FlickrApi
import io.truereactive.demo.flickr.data.api.model.FlickrPhoto
import io.truereactive.demo.flickr.data.api.model.PhotosResponse
import io.truereactive.demo.flickr.data.domain.PhotoModel
import io.truereactive.demo.flickr.data.domain.toDomain
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

}