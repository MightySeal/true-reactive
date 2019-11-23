package io.truereactive.core.abstraction

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.truereactive.core.reactiveui.ViewEvents
import io.truereactive.core.reactiveui.ViewState
import io.truereactive.library.BuildConfig
import java.util.*


// TODO: consider providing function like
//  fun logic(events: Observable<SearchViewEvents>, disposable)
//  so the object construction is predictable
abstract class BasePresenter() {
    internal val disposable = CompositeDisposable()

    fun onClear() {}

    fun Disposable.untilDead() = disposable.add(this)
}

internal fun BasePresenter.dispose() {
    disposable.dispose()
    onClear()
}

abstract class BaseFragment<VE : ViewEvents, M> : Fragment(), BaseHost<VE, M>,
    ViewDelegate<VE> by ViewDelegateImpl<VE>()

abstract class BaseActivity<VE : ViewEvents, M> : AppCompatActivity(), BaseHost<VE, M>,
    ViewDelegate<VE> by ViewDelegateImpl<VE>()

interface Renderer<M> {
    fun render(model: M)
}

internal interface BaseHost<VE : ViewEvents, M> : PresenterHost<VE, M>, ViewEventsHost<VE>,
    ViewDelegate<VE>, Renderer<M>

// TODO: Internal doesn't allow using destructuring declaration
data class ViewChannel<VE : ViewEvents, M>(
    internal val savedState: Observable<Bundle>,
    val state: Observable<ViewState>,
    internal val viewEvents: Observable<VE>,
    internal val renderer: Observable<Optional<out Renderer<M>>> // TODO use weakRef?
)

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

    companion object {
        internal const val VIEW_ID_KEY = "${BuildConfig.LIBRARY_PACKAGE_NAME}.host.id"
    }
}

internal class ViewDelegateImpl<VE : ViewEvents> : ViewDelegate<VE> {
    override lateinit var viewIdKey: String
    override lateinit var presenter: BasePresenter
}

data class Optional<T>(
    val value: T?,
    val logKey: String = UUID.randomUUID().toString()
)