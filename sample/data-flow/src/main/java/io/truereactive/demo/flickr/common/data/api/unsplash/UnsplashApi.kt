package io.truereactive.demo.flickr.common.data.api.unsplash

import io.truereactive.demo.flickr.common.data.api.unsplash.model.UnsplashPhoto
import io.truereactive.demo.flickr.common.data.api.unsplash.model.UnsplashSearchResult
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface UnsplashApi {

    @GET("/photos")
    suspend fun getPhotos(@Query("per_page") perPage: Int): List<UnsplashPhoto>

    @GET("/photos/{id}")
    suspend fun getPhoto(@Path("id") id: String): UnsplashPhoto

    @GET("/search/photos")
    suspend fun search(@Query("query") query: String): UnsplashSearchResult

    companion object {
        const val ENDPOINT = "https://api.unsplash.com/"
    }
}