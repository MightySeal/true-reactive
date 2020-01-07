package io.truereactive.demo.flickr.main.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.truereactive.demo.flickr.R
import io.truereactive.demo.flickr.common.BaseRecyclerAdapter
import io.truereactive.demo.flickr.common.data.domain.PhotoModel
import kotlinx.android.synthetic.main.list_item_photo.view.*

class PhotosAdapter(
    private val context: Context,
    private val glide: RequestManager,
    private val onClick: (PhotoModel) -> Unit
) : BaseRecyclerAdapter<PhotoVH, PhotoModel>() {

    // private val openSans = ResourcesCompat.getFont(context, R.font.font_opensans_normal)

    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoVH {
        return PhotoVH(inflater.inflate(R.layout.list_item_photo, parent, false)).apply {
            view.setOnClickListener {
                onClick(data[adapterPosition])
            }
        }
    }

    override fun onBindViewHolder(holder: PhotoVH, position: Int) {
        val item = data[position]

        glide
            .load(item.square)
            .thumbnail(glide.load(item.previewSquare))
            .placeholder(R.drawable.ic_image_preview_24)
            .error(R.drawable.ic_image_preview_24)
            .into(holder.image)

        /*if (position % 2 == 0) {
            holder.title.typeface = openSans
        } else {
            holder.title.typeface = Typeface.DEFAULT
        }*/

        holder.title.text = item.title
    }
}

class PhotoVH(val view: View) : RecyclerView.ViewHolder(view) {
    val image: ImageView = view.image
    val title: TextView = view.title
}