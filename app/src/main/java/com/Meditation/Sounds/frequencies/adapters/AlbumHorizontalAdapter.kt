package com.Meditation.Sounds.frequencies.adapters

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
import kotlinx.android.synthetic.main.item_album_horizontal.view.*
import java.io.File

/**
 * Created by Admin on 11/14/16.
 */

class AlbumHorizontalAdapter(internal var mContext: Context, internal var mData: List<Album>,internal var width : Int) : RecyclerView.Adapter<AlbumHorizontalAdapter.ViewHolder>() {
    private var mOnClickListener: IOnItemClicklistener? = null

    fun setOnItemListener(listener: IOnItemClicklistener) {
        mOnClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_album_horizontal, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = mData.get(position)
        if (!TextUtils.isEmpty(album.albumArt)) {
            Glide.with(mContext)
                    .applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.ic_album_placeholder))
                    .load(File(album.albumArt))
                    .into(holder.itemView.imgView)
        } else {
            holder.itemView.imgView.setImageResource(R.drawable.ic_album_default_small)
        }
        holder.itemView.imgView.layoutParams.width = width
        holder.itemView.imgView.layoutParams.height = width

    }

    override fun getItemCount(): Int {
        return mData.size
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }


    interface IOnItemClicklistener {
        fun onItemClick(position: Int)
    }

}