package io.truereactive.demo.flickr.common.data.api.flickr

import io.truereactive.demo.flickr.common.data.api.flickr.model.FlickrImageSizesResponse
import io.truereactive.demo.flickr.common.data.api.flickr.model.FlickrPhotoInfoResponse
import io.truereactive.demo.flickr.common.data.api.flickr.model.FlickrPhotosResponse
import retrofit2.http.GET
import retrofit2.http.Query

internal interface FlickrApi {

    @GET("?method=flickr.photos.getRecent&extras=url_sq,url_q")
    suspend fun getRecent(@Query("perPage") perPage: Int): FlickrPhotosResponse

    @GET("?method=flickr.photos.search&extras=url_sq,url_q")
    suspend fun search(@Query("text") search: String): FlickrPhotosResponse

    @GET("?method=flickr.photos.getInfo")
    suspend fun getInfo(@Query("photo_id") id: String): FlickrPhotoInfoResponse

    @GET("?method=flickr.photos.getSizes")
    suspend fun getImageSizes(@Query("photo_id") id: String): FlickrImageSizesResponse

    companion object {
        const val ENDPOINT = "https://www.flickr.com/services/rest/"
    }
}