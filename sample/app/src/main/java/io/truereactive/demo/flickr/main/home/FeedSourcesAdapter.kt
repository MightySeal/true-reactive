package io.truereactive.demo.flickr.main.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.truereactive.demo.flickr.main.home.tabs.SearchFragment
import timber.log.Timber

class FeedSourcesAdapter(fm: Fragment) : FragmentStateAdapter(fm) {
    private val sources: MutableList<String> = mutableListOf()

    constructor(fm: Fragment, sources: List<String>) : this(fm) {
        this.sources.addAll(sources)
    }

    fun setSources(sources: List<String>, notify: Boolean = true) {
        if (this.sources != sources) {
            this.sources.clear()
            this.sources.addAll(sources)
            if (notify) {
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return sources.size
    }

    override fun createFragment(position: Int): Fragment {
        return SearchFragment.newInstance(
            sources[position]
        )
    }

    fun getTabTitle(index: Int) = sources[index]
}