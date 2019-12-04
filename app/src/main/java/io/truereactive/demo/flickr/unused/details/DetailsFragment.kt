package io.truereactive.demo.flickr.unused.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.truereactive.core.abstraction.BaseFragment
import io.truereactive.core.abstraction.BasePresenter
import io.truereactive.core.abstraction.ViewChannel
import io.truereactive.demo.flickr.R
import kotlinx.android.synthetic.main.fragment_details.*

class DetailsFragment : BaseFragment<DetailsViewEvents, String>() {

    override fun createPresenter(
        viewChannel: ViewChannel<DetailsViewEvents, String>,
        args: Bundle?,
        savedState: Bundle?
    ): BasePresenter {
        return DetailsPresenter(
            viewChannel,
            args?.getString(TEXT_KEY)!!,
            savedState?.getLong(DetailsPresenter.TIME_KEY) ?: 0L
        )
    }

    override fun createViewHolder(view: View): DetailsViewEvents {
        return DetailsViewEvents(view)
    }

    override fun render(model: String) {
        label.text = model
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    companion object {
        private const val TEXT_KEY = "text_key"
        fun newInstance(text: String): DetailsFragment = DetailsFragment().also { fr ->
            fr.arguments = Bundle().apply {
                putString(TEXT_KEY, text)
            }
        }
    }
}
