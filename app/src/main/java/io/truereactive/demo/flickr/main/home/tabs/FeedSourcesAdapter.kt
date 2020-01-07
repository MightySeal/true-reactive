package io.truereactive.demo.flickr.main.home.tabs

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.truereactive.demo.flickr.main.home.SearchFragment

class FeedSourcesAdapter(fm: Fragment) : FragmentStateAdapter(fm) {

    private val sources: MutableList<String> = mutableListOf()

    fun setSources(sources: List<String>) {
        this.sources.clear()
        this.sources.addAll(sources)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return sources.size
    }

    override fun createFragment(position: Int): Fragment {
        return SearchFragment.newInstance(sources[position])
    }

    fun getTabTitle(index: Int) = sources[index]
}