package io.truereactive.library.flow

import android.app.Activity
import androidx.fragment.app.Fragment
import io.truereactive.library.core.ViewEvents

internal inline fun Activity.executeIfBase(action: (BaseActivity<ViewEvents, Any>) -> Unit) {
    (this as? BaseActivity<ViewEvents, Any>)?.let(action)
}

internal inline fun Fragment.executeIfBase(action: (BaseFragment<ViewEvents, Any>) -> Unit) {
    // TODO: get rid of unchecked with some magic
    (this as? BaseFragment<ViewEvents, Any>)?.let(action)
}