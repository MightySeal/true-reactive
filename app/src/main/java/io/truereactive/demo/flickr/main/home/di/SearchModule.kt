package io.truereactive.demo.flickr.main.home.di

import dagger.Module
import dagger.Subcomponent
import io.truereactive.demo.flickr.main.home.SearchFragment
import javax.inject.Scope

@SearchComponent.SearchScope
@Subcomponent(modules = [SearchModule::class])
interface SearchComponent {
    fun inject(searchFragment: SearchFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(): SearchComponent
    }

    @Scope
    annotation class SearchScope

}

@Module
class SearchModule