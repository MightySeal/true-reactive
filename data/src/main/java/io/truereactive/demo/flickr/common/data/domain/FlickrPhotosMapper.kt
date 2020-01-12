package io.truereactive.demo.flickr.common.data.domain

import io.truereactive.demo.flickr.common.data.api.flickr.model.FlickrPhoto
import io.truereactive.demo.flickr.common.data.api.flickr.model.FullFlickrPhoto
import io.truereactive.demo.flickr.common.data.api.flickr.model.ImageSize

internal fun FlickrPhoto.toDomain(): PhotoModel = PhotoModel(
    id = id,
    owner = owner,
    // secret = secret,
    // server = server,
    // farm = farm,
    title = title,
    // ispublic = ispublic,
    // isfriend = isfriend,
    // isfamily = isfamily,
    previewSquare = previewSquare,
    square = square,
    source = Source.Flickr
)

internal fun FullFlickrPhoto.toDomain(): PhotoModel = PhotoModel(
    id = id,
    owner = "TODO: owner",
    // secret = secret,
    // server = server,
    // farm = farm,
    title = "TODO: title",
    // ispublic = 1,
    // isfriend = 1,
    // isfamily = 1,
    previewSquare = urls.urlList.firstOrNull()?.content ?: "",
    square = urls.urlList.firstOrNull()?.content ?: "",
    source = Source.Flickr
)

internal fun ImageSize.toDomain(): PhotoSize = PhotoSize(
    imageWidth = width,
    imageHeight = height,
    staticUrl = source
)