package io.truereactive.demo.flickr.common.data.domain

import io.truereactive.demo.flickr.common.data.api.unsplash.model.FullUnsplashPhoto
import io.truereactive.demo.flickr.common.data.api.unsplash.model.UnsplashPhoto

internal fun UnsplashPhoto.toDomain(): PhotoModel = PhotoModel(
    id = this.id,
    square = this.urls.regular,
    previewSquare = this.urls.thumb,
    owner = this.user.username,
    title = null,
    source = Source.Unsplash
)

internal fun FullUnsplashPhoto.toDomain(): PhotoModel = PhotoModel(
    id = id,
    // secret = secret,
    // server = server,
    // farm = farm,
    // ispublic = 1,
    // isfriend = 1,
    // isfamily = 1,
    square = this.urls.regular,
    previewSquare = this.urls.thumb,
    owner = this.user.username,
    title = null,
    source = Source.Unsplash
)