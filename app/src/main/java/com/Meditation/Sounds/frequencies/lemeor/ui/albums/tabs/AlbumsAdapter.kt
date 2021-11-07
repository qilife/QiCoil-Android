package com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.loadImage
import kotlinx.android.synthetic.main.album_item.view.*

class AlbumsAdapter(
        private val mContext: Context,
        private var mData: ArrayList<Album>
) : RecyclerView.Adapter<AlbumsAdapter.ViewHolder>() {

    interface Listener {
        fun onClickItem(album: Album)
    }

    private var mListener: Listener? = null

    fun setOnClickListener(listener: Listener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.album_item, parent, false))
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = mData[position]

        holder.itemView.image.radius = mContext.resources!!.getDimensionPixelOffset(R.dimen.corner_radius_album)

       if (album.isUnlocked) { holder.itemView.image_lock.visibility = View.GONE }
       else {
           if (album.id == 221 || album.id == 1) {
               holder.itemView.image_lock.visibility = View.GONE
               holder.itemView.lock.visibility = View.VISIBLE
           } else {
               holder.itemView.image_lock.visibility = View.VISIBLE
               holder.itemView.lock.visibility = View.GONE
           }

       }
           //holder.itemView.image_lock.visibility = View.VISIBLE }
//        if (album.isUnlocked) { holder.itemView.lock.visibility = View.GONE }
  //      else { holder.itemView.lock.visibility = View.VISIBLE }
        loadImage(mContext, holder.itemView.image, album)

        holder.itemView.setOnClickListener { mListener?.onClickItem(album) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun setData(categoryList: List<Album>?) {
        mData = categoryList as ArrayList<Album>
        notifyDataSetChanged()
    }
}