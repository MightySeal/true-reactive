package io.truereactive.demo.flickr.common.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// TODO: Use inline classes
internal interface FlickrResponse {
    @Json(name = "page")
    val page: Int
    @Json(name = "pages")
    val pages: String   // TODO: make int
    @Json(name = "perPage")
    val perPage: Int
    @Json(name = "total")
    val total: String   // TODO: make int
    @Json(name = "stat")
    val stat: String // TODO: Enum
}

@JsonClass(generateAdapter = true)
internal data class FlickrPhoto(
    @Json(name = "id") val id: String,
    @Json(name = "owner") val owner: String, // TODO: Owner
    @Json(name = "secret") val secret: String,
    @Json(name = "server") val server: String,
    @Json(name = "farm") val farm: Int,
    @Json(name = "title") val title: String,
    @Json(name = "ispublic") val ispublic: Int, // TODO: make boolean
    @Json(name = "isfriend") val isfriend: Int, // TODO: make boolean
    @Json(name = "isfamily") val isfamily: Int, // TODO: make boolean
    @Json(name = "url_sq") val previewSquare: String,
    @Json(name = "url_q") val square: String
)

@JsonClass(generateAdapter = true)
internal data class FullFlickrPhoto(
    @Json(name = "id") val id: String,
    @Json(name = "secret") val secret: String,
    @Json(name = "server") val server: String,
    @Json(name = "farm") val farm: Int,
    @Json(name = "urls") val urls: Urls
)

// TODO: think of inheriting FlickrResponse
@JsonClass(generateAdapter = true)
internal data class PhotosResponse(
    @Json(name = "page") val page: Int,
    @Json(name = "pages") val pages: String,
    @Json(name = "perpage") val perPage: Int,
    @Json(name = "total") val total: String,
    @Json(name = "photo") val photoList: List<FlickrPhoto>
)

@JsonClass(generateAdapter = true)
internal data class FlickrPhotosResponse(
    @Json(name = "photos")
    val photos: PhotosResponse,
    @Json(name = "stat")
    val stat: String // TODO: Enum
)

@JsonClass(generateAdapter = true)
internal data class FlickrPhotoInfoResponse(
    @Json(name = "photo")
    val photo: FullFlickrPhoto,
    @Json(name = "stat")
    val stat: String // TODO: Enum
)

@JsonClass(generateAdapter = true)
data class Urls(
    @Json(name = "url")
    val urlList: List<FlickrUrl>
)

@JsonClass(generateAdapter = true)
data class FlickrUrl(
    @Json(name = "type")
    val type: String,
    @Json(name = "_content")
    val content: String
)