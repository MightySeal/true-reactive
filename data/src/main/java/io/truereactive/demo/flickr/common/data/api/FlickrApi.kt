package io.truereactive.demo.flickr.common.data.api

import io.reactivex.Single
import io.truereactive.demo.flickr.common.data.api.model.FlickrPhotoInfoResponse
import io.truereactive.demo.flickr.common.data.api.model.FlickrPhotosResponse
import retrofit2.http.GET
import retrofit2.http.Query

internal interface FlickrApi {

    @GET("?method=flickr.photos.getRecent&extras=url_sq")
    fun getRecent(): Single<FlickrPhotosResponse>

    @GET("?method=flickr.photos.search&extras=url_sq")
    fun search(@Query("text") search: String): Single<FlickrPhotosResponse>

    @GET("?method=flickr.photos.getInfo")
    fun getInfo(@Query("photo_id") id: String): Single<FlickrPhotoInfoResponse>

    /*@GET("?method=flickr.photos.getSizes")
    public fun getImageSizes(@Query("photo_id") id: String): Single<FlickrPhotosResponse>*/

    companion object {
        const val ENDPOINT = "https://www.flickr.com/services/rest/"
    }
}