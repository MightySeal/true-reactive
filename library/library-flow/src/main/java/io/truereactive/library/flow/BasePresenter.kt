package io.truereactive.library.flow

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import io.truereactive.library.core.CustomCache
import io.truereactive.library.core.Renderer
import io.truereactive.library.core.ViewEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope

abstract class BasePresenter : CoroutineScope by MainScope() {
    val parentJob = Job()

    open fun onClear() {}

    // fun Job.untilDead() = parentJob.add(this)
}

internal fun BasePresenter.dispose() {
    parentJob.cancel()
    onClear()
}

fun <VE : ViewEvents, M> BaseFragment<VE, M>.hasCache(): Boolean =
    CustomCache.hasKey(this.viewIdKey)

fun <VE : ViewEvents, M> BaseFragment<VE, M>.putCache(value: Any) =
    CustomCache.put(this.viewIdKey, value)

fun <VE : ViewEvents, M> BaseFragment<VE, M>.get() = CustomCache.get(this.viewIdKey)
fun <VE : ViewEvents, M> BaseFragment<VE, M>.remove() = CustomCache.remove(this.viewIdKey)

abstract class BaseFragment<VE : ViewEvents, M> : Fragment(), BaseHost<VE, M>,
    ViewDelegate<VE> by ViewDelegateImpl<VE>()

abstract class BaseActivity<VE : ViewEvents, M> : AppCompatActivity(), BaseHost<VE, M>,
    ViewDelegate<VE> by ViewDelegateImpl<VE>()

internal interface BaseHost<VE : ViewEvents, M> : PresenterHost<VE, M>, ViewEventsHost<VE>,
    ViewDelegate<VE>, Renderer<M>

internal interface PresenterHost<VE : ViewEvents, M> {

    fun createPresenter(
        viewChannel: ViewChannel<VE, M>,
        args: Bundle?,
        savedState: Bundle?
    ): BasePresenter

}

internal interface ViewEventsHost<VE : ViewEvents> {
    fun createViewHolder(view: View): VE
}

internal interface ViewDelegate<VE : ViewEvents> {
    var viewIdKey: String
    @Deprecated("Currently this property is not needed, so will be removed unless a use-case is found")
    var presenter: BasePresenter

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    companion object {
        const val VIEW_ID_KEY = "${BuildConfig.LIBRARY_PACKAGE_NAME}.host.id"
    }
}

internal class ViewDelegateImpl<VE : ViewEvents> : ViewDelegate<VE> {
    override lateinit var viewIdKey: String
    override lateinit var presenter: BasePresenter
}