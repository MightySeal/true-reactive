package io.truereactive.demo.flickr.unused.search

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.truereactive.core.abstraction.BaseFragment
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.demo.flickr.R
import io.truereactive.demo.flickr.unused.main.MainActivity
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

@ExperimentalCoroutinesApi
class SearchFragment : BaseFragment<InputViewEvents, SearchModel>() {

    override fun createPresenter(
        viewChannel: ViewChannel<InputViewEvents, SearchModel>,
        args: Bundle?,
        savedState: Bundle?
    ): InputPresenter {
        return InputPresenter(viewChannel)
    }

    // TODO: move navigation to separate place so this ref doesn't leak
    override fun createViewHolder(view: View): InputViewEvents =
        InputViewEvents(view, { text: String ->
            (requireActivity() as MainActivity).openDetails(text)
        }).also {
            Timber.i("Create ViewHolder")
        }

    override fun render(model: SearchModel) {
        searchMirror.text = model.text
    }

    fun a() {
        val res = ResultReceiver(Handler())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("Fragment destroyed called onDestroy")
    }

    companion object {
        private const val KEY_1 = "key"
    }
}

data class SearchModel(
    val text: String
)
