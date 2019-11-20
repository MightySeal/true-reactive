package io.truereactive.core.viewbinding

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

@ExperimentalCoroutinesApi
fun View.clicks(): Flow<Unit> = callbackFlow {

    this@clicks.setOnClickListener {
        offer(Unit)
    }

    awaitClose {
        this@clicks.setOnClickListener(null)
    }
}

// TODO: Filter emits that triggered by setText
@ExperimentalCoroutinesApi
fun EditText.inputFlow(): Flow<String> = callbackFlow<String> {
    val textListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            s?.toString()?.let(::offer)
        }
    }

    this@inputFlow.addTextChangedListener(textListener)
    awaitClose {
        this@inputFlow.removeTextChangedListener(textListener)
    }
}.flowOn(Dispatchers.Main.immediate)

fun EditText.input(): Observable<String> = Observable.create<String> { emitter ->
    val textListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            s?.toString()?.let(emitter::onNext)
        }
    }

    addTextChangedListener(textListener)
    emitter.setCancellable {
        removeTextChangedListener(textListener)
    }
}