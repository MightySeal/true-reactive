package io.truereactive.demo.flickr.main.home.di

import com.jakewharton.rxbinding3.appcompat.SearchViewQueryTextEvent
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import io.reactivex.Observable
import io.truereactive.demo.flickr.main.home.FeedFragment
import javax.inject.Scope

@HomeComponent.HomeScope
@Subcomponent(modules = [HomeModule::class])
interface HomeComponent {
    fun inject(fragment: FeedFragment)

    fun searchComponent(): SearchComponent.Factory

    fun exposeSearchEvents(): Observable<SearchViewQueryTextEvent>

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance searchEvents: Observable<SearchViewQueryTextEvent>): HomeComponent
    }

    @Scope
    annotation class HomeScope

}

@Module
class HomeModule