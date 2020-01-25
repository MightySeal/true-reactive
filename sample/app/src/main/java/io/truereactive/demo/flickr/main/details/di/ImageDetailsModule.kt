package io.truereactive.demo.flickr.main.details.di

import dagger.Module
import dagger.Subcomponent
import io.truereactive.demo.flickr.main.details.ImageDetailsFragment
import javax.inject.Scope

@ImageDetailsComponent.ImageDetailsScope
@Subcomponent(modules = [ImageDetailsModule::class])
interface ImageDetailsComponent {
    fun inject(searchFragment: ImageDetailsFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(): ImageDetailsComponent
    }

    @Scope
    annotation class ImageDetailsScope

}

@Module
class ImageDetailsModule