package io.truereactive.demo.flow

import android.app.Application
import io.truereactive.demo.flickr.common.data.di.DaggerDataComponent
import io.truereactive.demo.flow.di.ApplicationComponent
import io.truereactive.demo.flow.di.DaggerApplicationComponent
import io.truereactive.library.flow.ReactiveApplicationCompat
import timber.log.Timber

class FlowApplication : Application() {

    lateinit var reactiveApp: ReactiveApplicationCompat

    lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        appComponent = DaggerApplicationComponent.factory()
            .create(this, DaggerDataComponent.factory().create(this))

        Timber.i("========== Start app")

        reactiveApp = ReactiveApplicationCompat(this)
    }
}