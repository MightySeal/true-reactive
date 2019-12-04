package io.truereactive.demo.flickr

import android.app.Application
import io.truereactive.core.reactiveui.ReactiveApp
import io.truereactive.demo.flickr.common.data.di.DaggerDataComponent
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
            .create(this, DaggerDataComponent.factory().create(this))

        reactiveApp = ReactiveApp(this)
    }
}