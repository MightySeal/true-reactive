package io.truereactive.demo.flickr.common.data.repository

import io.truereactive.demo.flickr.common.data.api.flickr.FlickrApi
import io.truereactive.demo.flickr.common.data.api.flickr.model.FlickrPhoto
import io.truereactive.demo.flickr.common.data.api.flickr.model.ImageSize
import io.truereactive.demo.flickr.common.data.api.unsplash.UnsplashApi
import io.truereactive.demo.flickr.common.data.api.unsplash.model.UnsplashPhoto
import io.truereactive.demo.flickr.common.data.domain.PhotoModel
import io.truereactive.demo.flickr.common.data.domain.PhotoSize
import io.truereactive.demo.flickr.common.data.domain.Source
import io.truereactive.demo.flickr.common.data.domain.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class PhotosRepository @Inject internal constructor(
    private val flickrApi: FlickrApi,
    private val unsplashApi: UnsplashApi
) {
    private val sourcesCount = Source.values().size

    fun getRecent(perPage: Int = 100): Flow<List<PhotoModel>> {
        val perSource = perPage / sourcesCount

        return combine(
            flow { emit(flickrApi.getRecent(perSource)) }
                .flowOn(Dispatchers.IO)
                .map { it.photos.photoList.map(FlickrPhoto::toDomain) }
                .onStart { emit(emptyList()) },

            flow { emit(unsplashApi.getPhotos(perSource)) }
                .flowOn(Dispatchers.IO)
                .map { it.map(UnsplashPhoto::toDomain) }
                .onStart { emit(emptyList()) },
            { fromFlickr: List<PhotoModel>, fromUnsplash: List<PhotoModel> ->
                merge(fromFlickr, fromUnsplash)
            }).filter { it.isNotEmpty() }
    }

    fun search(text: String): Flow<List<PhotoModel>> = combine(
        flow { emit(flickrApi.search(text)) }
            .flowOn(Dispatchers.IO)
            .map { it.photos.photoList.map(FlickrPhoto::toDomain) }
            .onStart { emit(emptyList()) },

        flow { emit(unsplashApi.search(text)) }
            .flowOn(Dispatchers.IO)
            .map { it.results.map(UnsplashPhoto::toDomain) }
            .onStart { emit(emptyList()) }
    ) { fromFlickr: List<PhotoModel>, fromUnsplash: List<PhotoModel> ->
        merge(fromFlickr, fromUnsplash)
    }


    suspend fun getInfo(id: String, source: Source): PhotoModel = withContext(Dispatchers.IO) {
        when (source) {
            Source.Flickr -> {
                flickrApi.getInfo(id).photo.toDomain()
            }
            Source.Unsplash -> {
                unsplashApi.getPhoto(id).toDomain()
            }
        }
    }

    suspend fun getSizes(id: String): List<PhotoSize> = withContext(Dispatchers.IO) {
        flickrApi.getImageSizes(id).sizes.sizes.map(ImageSize::toDomain)
    }

    private fun merge(
        fromFlickr: List<PhotoModel>,
        fromUnsplash: List<PhotoModel>
    ): List<PhotoModel> {
        val result = mutableListOf<PhotoModel>()

        repeat(max(fromFlickr.size, fromUnsplash.size)) { index ->
            fromFlickr.getOrNull(index)?.let(result::add)
            fromUnsplash.getOrNull(index)?.let(result::add)
        }

        return result.toList()
    }
}
