package io.truereactive.demo.flickr.unused.search

import android.view.View
import io.truereactive.core.reactiveui.ViewEvents
import io.truereactive.core.viewbinding.input
import io.reactivex.Observable
import io.truereactive.core.viewbinding.inputFlow
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.rx2.asObservable

@ExperimentalCoroutinesApi
class InputViewEvents(
    view: View,
    val openDetails: (String) -> Unit
) : ViewEvents {
    val searchInput: Observable<String> =
        view.searchInput.inputFlow().distinctUntilChanged().asObservable()
}