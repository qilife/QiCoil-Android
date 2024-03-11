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
import com.Meditation.Sounds.frequencies.lemeor.tools.player.MusicRepository
import kotlinx.android.synthetic.main.item_album_track.view.divider
import kotlinx.android.synthetic.main.item_album_track.view.item_album_name
import kotlinx.android.synthetic.main.item_album_track.view.item_track_name
import kotlinx.android.synthetic.main.item_album_track.view.item_track_options

class RifeAdapter(
    private val onClickItem: (MusicRepository.Frequency, Int) -> Unit,
    private val onClickOptions: (MusicRepository.Frequency, Int) -> Unit
) : ListAdapter<MusicRepository.Frequency, RifeAdapter.ViewHolder>(FrequencyDiffCallback()) {
    private var selectedItem: MusicRepository.Frequency? = null
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): RifeAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_album_track, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RifeAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(item: MusicRepository.Frequency, position: Int) {
            if (position == itemCount - 1) {
                itemView.divider.visibility = View.INVISIBLE
            } else {
                itemView.divider.visibility = View.VISIBLE
            }
            itemView.item_track_name.text = item.frequency.toString()

            itemView.item_album_name.text = "03:00"
            itemView.updateView(item.apply {
                isSelected = index == selectedItem?.index
            })

            itemView.setOnClickListener {
                setSelectedItem(item)
                onClickItem.invoke(item, position)
            }

            itemView.item_track_options.setOnClickListener { onClickOptions.invoke(item, position) }
        }
    }


    private fun View.updateView(item: MusicRepository.Frequency) {
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
    fun setSelectedItem(item: MusicRepository.Frequency?) {
        selectedItem = item
        notifyDataSetChanged()
    }

    private class FrequencyDiffCallback : DiffUtil.ItemCallback<MusicRepository.Frequency>() {
        override fun areItemsTheSame(
            oldItem: MusicRepository.Frequency, newItem: MusicRepository.Frequency
        ): Boolean {
            return oldItem.index == newItem.index && oldItem.rifeId == newItem.rifeId
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: MusicRepository.Frequency, newItem: MusicRepository.Frequency
        ): Boolean {
            return oldItem == newItem
        }
    }
}