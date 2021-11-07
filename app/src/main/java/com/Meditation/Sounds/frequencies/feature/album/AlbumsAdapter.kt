package com.Meditation.Sounds.frequencies.feature.album

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.models.Album
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.album_item.view.*
import java.io.File

/**
 * Created by Admin on 11/14/16.
 */

class AlbumsAdapter(private val mContext: Context?, var data: List<Album>, private var isAdvanced: Boolean, private var mOnItemClickListener: IOnItemClickListener?) : RecyclerView.Adapter<AlbumsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.album_item, parent, false))
    }

    fun setIsAdvanced(advanced: Boolean) {
        this.isAdvanced = advanced
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.image.requestLayout()
        holder.itemView.image_lock.requestLayout()
        val album = data[position]

            holder.itemView.image.radius = mContext?.resources!!.getDimensionPixelOffset(R.dimen.corner_radius_album)

        if (album.isPurchase) {
            holder.itemView.image_lock.visibility = View.GONE
        } else {
            holder.itemView.image_lock.visibility = View.VISIBLE
        }


        if (!TextUtils.isEmpty(album.albumArt)) {
            if (album.albumArt!!.startsWith("http")) {
                Glide.with(holder.itemView.context)
                        .applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.ic_album_placeholder))
                        .load(album.albumArt)
                        .into(holder.itemView.image)
            } else {
                Glide.with(holder.itemView.context)
                        .applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.ic_album_placeholder))
                        .load(File(album.albumArt))
                        .into(holder.itemView.image)
            }
        } else {
            holder.itemView.image.setImageResource(R.drawable.ic_album_placeholder)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            itemView.setOnClickListener {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(data[layoutPosition], layoutPosition)
                }
            }
        }
    }

    interface IOnItemClickListener {
        fun onItemClick(album: Album, position: Int)
    }
}
