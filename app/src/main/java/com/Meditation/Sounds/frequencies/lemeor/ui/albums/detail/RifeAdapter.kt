package com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.tools.player.MusicRepository
import kotlinx.android.synthetic.main.item_album_track.view.*

class RifeAdapter(
    private val mContext: Context,
    private var mData: List<MusicRepository.Frequency>,
    private val listener: (Int) -> Unit,
) : RecyclerView.Adapter<RifeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_album_track, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val frequency = mData[position]

        if (frequency.isSelected) {
            holder.itemView.item_track_name.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.colorPrimary
                )
            )
            holder.itemView.item_album_name.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.colorPrimary
                )
            )
        } else {
            holder.itemView.item_track_name.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    android.R.color.white
                )
            )
            holder.itemView.item_album_name.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    android.R.color.white
                )
            )
        }

        //  holder.itemView.item_track_name.text = convertedTrackName(mAlbum, track)


        holder.itemView.item_track_name.text = frequency.frequency.toString()

        holder.itemView.item_album_name.text = "03:00"

        holder.itemView.setOnClickListener { listener.invoke(position) }

        if (position == mData.size - 1) {
            holder.itemView.divider.visibility = View.INVISIBLE
        } else {
            holder.itemView.divider.visibility = View.VISIBLE
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun setData(frequencyList: List<MusicRepository.Frequency>?) {
        mData = frequencyList as ArrayList<MusicRepository.Frequency>
        notifyDataSetChanged()
    }

    fun setSelected(selectedPosition: Int) {
        mData.forEach { it.isSelected = false }
        mData[selectedPosition].isSelected = true
        notifyDataSetChanged()
    }
}