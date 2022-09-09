package com.Meditation.Sounds.frequencies.lemeor.tools.downloader

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = mData[position]
        holder.itemView.item_track_name.text = track.name
        holder.itemView.item_track_progress.progress = track.progress
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun setData(tagList: List<Track>?) {
        mData = tagList as ArrayList<Track>
        notifyDataSetChanged()
    }

    fun updateProgress(position: Int, progress: Int) {
        mData[position].progress = progress
        notifyDataSetChanged()
    }
}