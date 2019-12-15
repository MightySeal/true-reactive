package io.truereactive.demo.flickr.common.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class FlickrImageSizesResponse(
    @Json(name = "sizes")
    val sizes: ImageSizes,
    @Json(name = "stat")
    val stat: String // TODO: Enum
)

@JsonClass(generateAdapter = true)
internal data class ImageSizes(
    @Json(name = "candownload")
    val canDownload: Int, // TODO: boolean
    @Json(name = "size")
    val sizes: List<ImageSize>
)

@JsonClass(generateAdapter = true)
internal data class ImageSize(
    @Json(name = "label")
    val label: String, // TODO: enum
    @Json(name = "width")
    val width: Int,
    @Json(name = "height")
    val height: Int,
    @Json(name = "source")
    val source: String,
    @Json(name = "url")
    val url: String,
    @Json(name = "media")
    val media: String   // TODO: enum
)