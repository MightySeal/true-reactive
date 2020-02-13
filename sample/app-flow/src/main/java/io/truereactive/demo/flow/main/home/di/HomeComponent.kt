package io.truereactive.demo.flow.main.home.di

import com.jakewharton.rxbinding3.appcompat.SearchViewQueryTextEvent
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import io.truereactive.demo.flow.main.home.FeedFragment
import kotlinx.coroutines.flow.Flow
import javax.inject.Scope

@HomeComponent.HomeScope
@Subcomponent(modules = [HomeModule::class])
interface HomeComponent {
    fun inject(fragment: FeedFragment)

    fun searchComponent(): SearchComponent.Factory

    fun exposeSearchEvents(): Flow<SearchViewQueryTextEvent>

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance searchEvents: Flow<SearchViewQueryTextEvent>): HomeComponent
    }

    @Scope
    annotation class HomeScope

}

@Module
class HomeModule