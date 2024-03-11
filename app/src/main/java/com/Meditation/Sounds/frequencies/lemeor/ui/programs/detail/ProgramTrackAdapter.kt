package com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.loadImage
import com.Meditation.Sounds.frequencies.lemeor.tools.player.MusicRepository
import kotlinx.android.synthetic.main.item_program_track.view.divider
import kotlinx.android.synthetic.main.item_program_track.view.item_album_name
import kotlinx.android.synthetic.main.item_program_track.view.item_track_image
import kotlinx.android.synthetic.main.item_program_track.view.item_track_name
import kotlinx.android.synthetic.main.item_program_track.view.item_track_options

class ProgramTrackAdapter(
    private val onClickItem: (item: Search) -> Unit,
    private val onCLickOptions: (item: Search) -> Unit
) : ListAdapter<Search, ProgramTrackAdapter.ViewHolder>(SearchDiffCallback()) {
    private var selectedItem: Search? = null
    var isMy = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_program_track, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ProgramTrackAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(item: Search, position: Int) {
            if (isMy) {
                itemView.item_track_options.visibility = View.VISIBLE
            } else {
                itemView.item_track_options.visibility = View.GONE
            }
            if (position == itemCount - 1) {
                itemView.divider.visibility = View.INVISIBLE
            } else {
                itemView.divider.visibility = View.VISIBLE
            }
            itemView.item_track_options.setOnClickListener {
                onCLickOptions.invoke(item)
            }
            itemView.setOnClickListener {
                setSelectedItem(item)
                onClickItem.invoke(item)
            }
            when (item.obj) {
                is Track -> {
                    val t = item.obj as Track
                    t.isSelected = position == selectedItem?.id
                    itemView.updateUIForTrack(t)
                }

                is MusicRepository.Frequency -> {
                    val f = item.obj as MusicRepository.Frequency
                    f.isSelected = position == selectedItem?.id
                    itemView.updateUIForFrequency(f)
                }
            }
        }
    }

    private fun View.updateUIForTrack(track: Track) {
        item_track_name.setTextColor(
            ContextCompat.getColor(
                context, if (track.isSelected) R.color.colorPrimary else android.R.color.white
            )
        )
        item_album_name.setTextColor(
            ContextCompat.getColor(
                context, if (track.isSelected) R.color.colorPrimary else android.R.color.white
            )
        )
        item_track_name.text = track.name
        track.album?.let { album ->
            loadImage(context, item_track_image, album)
            item_album_name.text = album.name
        }
    }

    private fun View.updateUIForFrequency(frequency: MusicRepository.Frequency) {
        item_track_name.setTextColor(
            ContextCompat.getColor(
                context, if (frequency.isSelected) R.color.colorPrimary else android.R.color.white
            )
        )
        item_album_name.setTextColor(
            ContextCompat.getColor(
                context, if (frequency.isSelected) R.color.colorPrimary else android.R.color.white
            )
        )
        item_track_image.setImageResource(R.drawable.frequency)
        item_track_name.text = context.getString(R.string.navigation_lbl_rife)
        item_album_name.text = frequency.frequency.toString()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedItem(item: Search?) {
        selectedItem = item
        notifyDataSetChanged()
    }
    fun getItemSelected() = selectedItem

    private class SearchDiffCallback : DiffUtil.ItemCallback<Search>() {
        override fun areItemsTheSame(oldItem: Search, newItem: Search): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Search, newItem: Search): Boolean {
            return oldItem == newItem
        }
    }
}