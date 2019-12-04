package io.truereactive.demo.flickr.di

import android.app.Application
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import io.truereactive.demo.flickr.FlickrApplication
import io.truereactive.demo.flickr.common.data.di.DataComponent
import io.truereactive.demo.flickr.main.home.di.SearchComponent
import javax.inject.Scope

@ApplicationComponent.AppScope
@Component(
    modules = [ApplicationModule::class],
    dependencies = [DataComponent::class]
)
interface ApplicationComponent {

    fun searchComponent(): SearchComponent.Factory

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: FlickrApplication, dataComponent: DataComponent): ApplicationComponent
    }

    @Scope
    annotation class AppScope
}

@Module(subcomponents = [SearchComponent::class])
abstract class ApplicationModule {

    @Binds
    abstract fun bindApplication(app: FlickrApplication): Application
}
