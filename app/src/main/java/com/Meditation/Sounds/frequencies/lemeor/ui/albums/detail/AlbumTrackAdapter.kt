package com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.convertedTrackName
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import kotlinx.android.synthetic.main.item_album_track.view.*

class AlbumTrackAdapter(
        private val mContext: Context,
        private var mData: List<Track>,
        private var mAlbum: Album
) : RecyclerView.Adapter<AlbumTrackAdapter.ViewHolder>() {

    interface Listener {
        fun onTrackClick(track: Track, i: Int, isDownloaded: Boolean)
        fun onTrackOptions(track: Track, i: Int)
    }

    private var mListener: Listener? = null

    fun setOnClickListener(listener: Listener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_album_track, parent, false))
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = mData[position]

        if (track.isSelected) {
            holder.itemView.item_track_name.setTextColor(getColor(mContext, R.color.colorPrimary))
            holder.itemView.item_album_name.setTextColor(getColor(mContext, R.color.colorPrimary))
        } else {
            holder.itemView.item_track_name.setTextColor(getColor(mContext, android.R.color.white))
            holder.itemView.item_album_name.setTextColor(getColor(mContext, android.R.color.white))
        }

      //  holder.itemView.item_track_name.text = convertedTrackName(mAlbum, track)

        holder.itemView.item_track_name.text = track.name

        holder.itemView.item_album_name.text = mAlbum.name
        holder.itemView.item_track_options.setOnClickListener {  mListener?.onTrackOptions(track, position) }
        holder.itemView.setOnClickListener { mListener?.onTrackClick(track, position, mAlbum.isDownloaded) }

        if (position == mData.size - 1) {
            holder.itemView.divider.visibility = View.INVISIBLE
        } else {
            holder.itemView.divider.visibility = View.VISIBLE
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun setData(trackList: List<Track>?) {
        mData = trackList as ArrayList<Track>
        notifyDataSetChanged()
    }

    fun setSelected(selectedPosition: Int) {
        mData.forEach { it.isSelected = false }
        mData[selectedPosition].isSelected = true
        notifyDataSetChanged()
    }
}