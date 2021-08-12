package com.Meditation.Sounds.frequencies.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.models.Album
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.playlist_album_item.view.*
import java.io.File

/**
 * Created by Admin on 11/14/16.
 */

class PlaylistAlbumAdapter(internal var mContext: Context, internal var mData: List<Album>) : RecyclerView.Adapter<PlaylistAlbumAdapter.ViewHolder>() {
    private var mOnClickListener: IOnItemClicklistener? = null

    fun setOnItemListener(listener: IOnItemClicklistener) {
        mOnClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.playlist_album_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = mData.get(position)
        holder.name.text = album.name
        holder.name.isSelected = false
        if (album.isPurchase) {
            holder.imageLock.visibility = View.GONE
            holder.name.isSelected = true
        } else {
//            if (position == 0 && !isAdvanced){
//                holder.imageLock.visibility = View.GONE
//                holder.name.isSelected = true
//            }else{
//                holder.imageLock.visibility = View.VISIBLE
//                holder.name.isSelected = false
//            }
            holder.imageLock.visibility = View.VISIBLE
            holder.name.isSelected = false
        }
        if (!TextUtils.isEmpty(album.albumArt)) {
            Glide.with(mContext)
                    .applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.ic_album_placeholder))
                    .load(File(album.albumArt))
                    .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.ic_album_default_small)
        }
//        if(album.image != -1) {
//            holder.image.setImageResource(album.image)
//        } else {
//            holder.image.setImageBitmap(null)
//        }
//        if (position == mData.size - 1) {
//            holder.viewLine.visibility = View.GONE
//        } else {
//            holder.viewLine.visibility = View.VISIBLE
//        }

        holder.viewLine.visibility = View.VISIBLE
        holder.itemView.setOnClickListener {
            mOnClickListener!!.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: TextView
        var image: ImageView
        var viewLine: View
        var imageLock: ImageView


        init {
            name = view.tv_name
            image = view.image
            viewLine = view.view_line
            imageLock = view.image_lock
        }
    }


    interface IOnItemClicklistener {
        fun onItemClick(position: Int)
    }

}