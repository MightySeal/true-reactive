package io.truereactive.core.setup.activity

import android.app.Application
import io.truereactive.core.reactiveui.ReactiveApp
import timber.log.Timber

class TestApplication : Application() {

    lateinit var reactiveApp: ReactiveApp

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        super.onCreate()

        reactiveApp = ReactiveApp(this)
    }
}