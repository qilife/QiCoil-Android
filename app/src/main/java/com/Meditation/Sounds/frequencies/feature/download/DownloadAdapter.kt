package com.Meditation.Sounds.frequencies.feature.download

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.StringsUtils
import kotlinx.android.synthetic.main.item_download.view.*
import java.net.URLDecoder

class DownloadAdapter(var data:ArrayList<DownloadItem>): RecyclerView.Adapter<DownloadAdapter.ViewHolder>(){
    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_download, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.itemView.item_track_name.text = URLDecoder
                .decode(StringsUtils.getFileName(item.url), Constants.CHARSET)
                .replace(" - David Wong", "")
                .replace(" - David Sereda", "")
                .replace(".mp3", "")
        holder.itemView.item_track_progress.progress = item.progress
    }

   inner class ViewHolder(view:View): RecyclerView.ViewHolder(view)
}
