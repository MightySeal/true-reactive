package io.truereactive.demo.flickr.di

import dagger.BindsInstance
import dagger.Component
import dagger.Module
import io.truereactive.demo.flickr.FlickrApplication
import io.truereactive.demo.flickr.data.di.NetworkComponent
import io.truereactive.demo.flickr.main.home.di.SearchComponent
import javax.inject.Scope

@ApplicationComponent.AppScope
@Component(
    modules = [ApplicationModule::class],
    dependencies = [NetworkComponent::class]
)
interface ApplicationComponent {

    fun searchComponent(): SearchComponent.Factory

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: FlickrApplication, dataComponent: NetworkComponent): ApplicationComponent
    }

    @Scope
    annotation class AppScope
}

@Module(subcomponents = [SearchComponent::class])
abstract class ApplicationModule {

}
