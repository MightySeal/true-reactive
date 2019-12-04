package io.truereactive.demo.flickr.common.data.domain

import io.truereactive.demo.flickr.common.data.api.model.FlickrPhoto

internal fun FlickrPhoto.toDomain(): PhotoModel = PhotoModel(
    id = id,
    owner = owner,
    secret = secret,
    server = server,
    farm = farm,
    title = title,
    ispublic = ispublic,
    isfriend = isfriend,
    isfamily = isfamily,
    previewSquare = previewSquare
)