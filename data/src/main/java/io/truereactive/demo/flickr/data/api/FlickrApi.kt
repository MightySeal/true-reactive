package io.truereactive.demo.flickr.data.api

import io.reactivex.Single
import io.truereactive.demo.flickr.data.api.model.FlickrPhotosResponse
import retrofit2.http.GET
import retrofit2.http.Query

/*
https://www.flickr.com/services/rest/?method=flickr.photos.getPopular&api_key=47d4c895a65f0d872490845f771d3c0b&format=json&nojsoncallback=1&auth_token=72157711832920522-eb70a1989058ab23&api_sig=126b36e1d957a844512027c60607fb42
*/
internal interface FlickrApi {

    /*
    * https://www.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=438aced599008323f57e75d7b5b113bf&format=json&nojsoncallback=1
    * */

    @GET("?method=flickr.photos.getRecent&extras=url_sq")
    public fun getRecent(): Single<FlickrPhotosResponse>

    /*
    * https://www.flickr.com/services/rest/?method=flickr.photos.search&api_key=438aced599008323f57e75d7b5b113bf&text=London&format=json&nojsoncallback=1
    * */
    @GET("?method=flickr.photos.search&extras=url_sq")
    public fun search(@Query("text") search: String): Single<FlickrPhotosResponse>

    companion object {
        const val ENDPOINT = "https://www.flickr.com/services/rest/"
    }
}