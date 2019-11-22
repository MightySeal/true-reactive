package io.truereactive.demo.flickr

import android.app.Application
import io.truereactive.core.reactiveui.ReactiveApp
import io.truereactive.demo.flickr.data.di.DaggerNetworkComponent
import io.truereactive.demo.flickr.di.ApplicationComponent
import io.truereactive.demo.flickr.di.DaggerApplicationComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

@ExperimentalCoroutinesApi
open class FlickrApplication : Application() {

    lateinit var reactiveApp: ReactiveApp

    lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        super.onCreate()

        appComponent = DaggerApplicationComponent.factory()
            .create(this, DaggerNetworkComponent.factory().create())

        reactiveApp = ReactiveApp(this)
    }
}