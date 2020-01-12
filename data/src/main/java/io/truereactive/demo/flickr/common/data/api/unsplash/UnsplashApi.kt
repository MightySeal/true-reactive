package io.truereactive.demo.flickr.common.data.api.unsplash

import io.reactivex.Single
import io.truereactive.demo.flickr.common.data.api.unsplash.model.UnsplashPhoto
import io.truereactive.demo.flickr.common.data.api.unsplash.model.UnsplashSearchResult
import retrofit2.http.GET
import retrofit2.http.Query

internal interface UnsplashApi {

    @GET("/photos")
    fun getPhotos(@Query("per_page") perPage: Int): Single<List<UnsplashPhoto>>

    @GET("/photos/{id}")
    fun getPhoto(@Query("id") id: String): Single<UnsplashPhoto>

    @GET("/search/photos")
    fun search(@Query("query") query: String): Single<UnsplashSearchResult>

    companion object {
        const val ENDPOINT = "https://api.unsplash.com/"
    }
}