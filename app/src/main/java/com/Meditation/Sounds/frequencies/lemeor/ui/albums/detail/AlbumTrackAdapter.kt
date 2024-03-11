package com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import kotlinx.android.synthetic.main.item_album_track.view.divider
import kotlinx.android.synthetic.main.item_album_track.view.item_album_name
import kotlinx.android.synthetic.main.item_album_track.view.item_track_name
import kotlinx.android.synthetic.main.item_album_track.view.item_track_options

class AlbumTrackAdapter(
    private val onClickItem: (item: Track, pos: Int, isDownloaded: Boolean) -> Unit,
    private val onClickOptions: (item: Track, pos: Int) -> Unit
) : ListAdapter<Track, AlbumTrackAdapter.ViewHolder>(TrackDiffCallback()) {
    var album: Album? = null

    private var selectedItem: Track? = null

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AlbumTrackAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_album_track, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AlbumTrackAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(item: Track, position: Int) {
            if (position == itemCount - 1) {
                itemView.divider.visibility = View.INVISIBLE
            } else {
                itemView.divider.visibility = View.VISIBLE
            }

            itemView.item_track_name.text = item.name

            itemView.item_album_name.text = album?.name ?: ""

            itemView.updateView(item.apply {
                isSelected = id == selectedItem?.id
            })

            itemView.item_track_options.setOnClickListener {
                onClickOptions.invoke(item, position)
            }

            itemView.setOnClickListener {
                setSelectedItem(item)
                onClickItem.invoke(item, position, album?.isDownloaded ?: false)
            }
        }
    }

    private fun View.updateView(item: Track) {
        item_track_name.setTextColor(
            ContextCompat.getColor(
                context, if (item.isSelected) R.color.colorPrimary else android.R.color.white
            )
        )
        item_album_name.setTextColor(
            ContextCompat.getColor(
                context, if (item.isSelected) R.color.colorPrimary else android.R.color.white
            )
        )
    }


    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedItem(item: Track?) {
        selectedItem = item
        notifyDataSetChanged()
    }

    private class TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem == newItem
        }
    }

}