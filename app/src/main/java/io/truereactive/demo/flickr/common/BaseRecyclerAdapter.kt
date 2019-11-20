package io.truereactive.demo.flickr.common

import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerAdapter<VH: RecyclerView.ViewHolder, D>: RecyclerView.Adapter<VH>() {
    private val internalData = mutableListOf<D>()
    protected val data: List<D> = internalData

    override fun getItemCount(): Int = internalData.size

    fun replace(newData: List<D>) {
        internalData.clear()
        internalData.addAll(newData)
        notifyDataSetChanged()
    }
}