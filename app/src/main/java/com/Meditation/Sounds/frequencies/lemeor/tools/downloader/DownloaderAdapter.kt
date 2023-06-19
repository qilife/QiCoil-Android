package com.Meditation.Sounds.frequencies.lemeor.tools.downloader

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.downloadErrorTracks
import kotlinx.android.synthetic.main.item_download.view.*

class DownloaderAdapter(
        private val mContext: Context,
        private var mData: List<Track>,
) : RecyclerView.Adapter<DownloaderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_download, parent, false))
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = mData[position]
        holder.itemView.item_track_name.text = track.name
        holder.itemView.item_track_progress.progress = track.progress
        if (downloadErrorTracks?.contains(track.id.toString()) == true) {
            holder.itemView.item_track_progress.progressDrawable =  ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_progress_bar_error)
        } else {
            holder.itemView.item_track_progress.progressDrawable =  ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_progress_bar)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    @SuppressLint("NotifyDataSetChanged")
    fun setData(tagList: List<Track>?) {
        mData = tagList as ArrayList<Track>
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateProgress(position: Int, progress: Int) {
        mData[position].progress = progress
        notifyDataSetChanged()
    }
}