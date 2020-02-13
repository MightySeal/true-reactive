package io.truereactive.demo.core

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections.emptyList

abstract class BaseRecyclerAdapter<VH : RecyclerView.ViewHolder, D> : RecyclerView.Adapter<VH>() {

    private var internalData: RecyclerData<D> = RecyclerData(emptyList(), null)
    protected val data: List<D>
        get() = internalData.data

    override fun getItemCount(): Int = internalData.data.size

    fun replace(newData: RecyclerData<D>) {
        internalData = newData
        if (newData.diffResult != null) {

            // TODO: figure out `Inconsistency detected. Invalid item position 10(offset:110).state:110`
            // newData.diffResult.dispatchUpdatesTo(this)
            notifyDataSetChanged()
        } else {
            notifyDataSetChanged()
        }
    }
}

class RecyclerData<D>(
    val data: List<D>,
    val diffResult: DiffUtil.DiffResult?
)