package io.truereactive.demo.flickr

import leakcanary.AppWatcher
import leakcanary.LeakCanary
import shark.AndroidObjectInspectors
import timber.log.Timber

class DebugFlickrApplication : FlickrApplication() {

    override fun onCreate() {
        super.onCreate()
        Timber.i("Debug app")
        AppWatcher.config = AppWatcher.config.copy(watchFragmentViews = false)
        LeakCanary.config = LeakCanary.config.copy(
            objectInspectors = AndroidObjectInspectors.appDefaults + reactiveApp.optionalReporter
        )
    }
}
