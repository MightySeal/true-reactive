package io.truereactive.demo.flickr.common.data.api.unsplash.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class UnsplashPhoto(
    @Json(name = "id") val id: String,
    @Json(name = "urls") val urls: UnsplashUrls,
    @Json(name = "user") val user: UnsplashUser
)

@JsonClass(generateAdapter = true)
internal data class UnsplashUrls(
    @Json(name = "raw") val raw: String,
    @Json(name = "full") val full: String,
    @Json(name = "regular") val regular: String,
    @Json(name = "small") val small: String,
    @Json(name = "thumb") val thumb: String
)

@JsonClass(generateAdapter = true)
internal data class UnsplashUser(
    @Json(name = "id") val id: String,
    @Json(name = "username") val username: String,
    @Json(name = "name") val name: String,
    @Json(name = "first_name") val firstName: String?,
    @Json(name = "last_name") val lastName: String?
)

@JsonClass(generateAdapter = true)
internal data class FullUnsplashPhoto(
    @Json(name = "id") val id: String,
    @Json(name = "urls") val urls: UnsplashUrls,
    @Json(name = "user") val user: UnsplashUser
)

@JsonClass(generateAdapter = true)
internal data class UnsplashSearchResult(
    @Json(name = "total") val total: Int,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "results") val results: List<UnsplashPhoto>
)