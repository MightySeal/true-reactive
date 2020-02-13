package io.truereactive.demo.flickr

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import io.truereactive.demo.flickr.common.data.di.DaggerDataComponent
import io.truereactive.demo.flickr.di.ApplicationComponent
import io.truereactive.demo.flickr.di.DaggerApplicationComponent
import io.truereactive.library.rx.reactiveui.ReactiveApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

@ExperimentalCoroutinesApi
open class FlickrApplication : Application() {

    lateinit var reactiveApp: ReactiveApp

    lateinit var appComponent: ApplicationComponent

    private var createTime: Long = 0L

    override fun onCreate() {
        super.onCreate()

        createTime = System.currentTimeMillis()

        // Trace.beginSection("TRACE_STARTUP")
        // Debug.startMethodTracing("TRACE_STARTUP")

        Timber.plant(Timber.DebugTree())
        appComponent = DaggerApplicationComponent.factory()
            .create(this, DaggerDataComponent.factory().create(this))

        reactiveApp = ReactiveApp(this)
    }

    fun trackTimePassed(tag: String) {
        val timePassed = System.currentTimeMillis() - createTime
        Timber.i("========== $tag, time: $timePassed")
    }
}

fun Context.app(): FlickrApplication = applicationContext as FlickrApplication
fun Fragment.app(): FlickrApplication = requireActivity().app()