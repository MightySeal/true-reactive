package io.truereactive.demo.flickr.unused.search

import android.view.View
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.truereactive.core.reactiveui.ViewEvents
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class InputViewEvents(
    view: View,
    val openDetails: (String) -> Unit
) : ViewEvents {
    val searchInput: Observable<String> =
        view.searchInput.textChanges().map(CharSequence::toString)
}