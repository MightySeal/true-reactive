package io.truereactive.demo.flickr.common.data.domain

enum class Source {
    Flickr,
    Unsplash
}

data class PhotoModel(
    val id: String,
    val owner: String, // TODO: Owner
    // val secret: String,
    // val server: String,
    // val farm: Int,
    val title: String?,
    // val ispublic: Int, // TODO: make boolean
    // val isfriend: Int, // TODO: make boolean
    // val isfamily: Int, // TODO: make boolean
    val previewSquare: String,
    val square: String,
    val source: Source
)

data class PhotoSize(
    val imageWidth: Int,
    val imageHeight: Int,
    val staticUrl: String
)