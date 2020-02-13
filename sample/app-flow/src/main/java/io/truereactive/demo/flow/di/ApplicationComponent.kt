package io.truereactive.demo.flow.di

import android.app.Application
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import io.truereactive.demo.flickr.common.data.di.DataComponent
import io.truereactive.demo.flow.FlowApplication
import io.truereactive.demo.flow.main.home.di.HomeComponent
import javax.inject.Scope

@ApplicationComponent.AppScope
@Component(
    modules = [ApplicationModule::class],
    dependencies = [DataComponent::class]
)
interface ApplicationComponent {

    fun homeComponent(): HomeComponent.Factory

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: FlowApplication, dataComponent: DataComponent): ApplicationComponent
    }

    @Scope
    annotation class AppScope
}

@Module(subcomponents = [HomeComponent::class])
abstract class ApplicationModule {

    @Binds
    abstract fun bindApplication(app: FlowApplication): Application
}
