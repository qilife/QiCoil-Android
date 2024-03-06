package com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.convertedTrackName
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.loadImage
import com.Meditation.Sounds.frequencies.lemeor.tools.player.MusicRepository
import kotlinx.android.synthetic.main.item_program_track.view.*
import kotlinx.coroutines.*

class ProgramTrackAdapter(
    private val mContext: Context,
    private var mData: List<Any>,
    private var isMy: Boolean
) : RecyclerView.Adapter<ProgramTrackAdapter.ViewHolder>() {

    interface Listener {
        fun onTrackClick(track: Any, i: Int)
        fun onTrackOptions(track: Any, i: Int)
    }

    private var mListener: Listener? = null

    fun setOnClickListener(listener: Listener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_program_track, parent, false))
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = mData[position]

        if (isMy) {
            holder.itemView.item_track_options.visibility = View.VISIBLE
        } else {
            holder.itemView.item_track_options.visibility = View.GONE
        }
        if (track is Track) {
            if (track.isSelected) {
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

            CoroutineScope(Dispatchers.IO).launch {
                val album = DataBase.getInstance(mContext).albumDao()
                    .getAlbumById(track.albumId, track.category_id)

                withContext(Dispatchers.Main) {
                    loadImage(mContext, holder.itemView.item_track_image, album!!)
                    holder.itemView.item_track_name.text = convertedTrackName(album, track)
                    holder.itemView.item_album_name.text = album.name
                }
            }

            holder.itemView.item_track_options.setOnClickListener {
                mListener?.onTrackOptions(
                    track,
                    position
                )
            }
            holder.itemView.setOnClickListener { mListener?.onTrackClick(track, position) }
        } else if (track is MusicRepository.Frequency) {
            if (track.isSelected) {
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

            holder.itemView.item_track_image.setImageResource(R.drawable.frequency)
            holder.itemView.item_track_name.text = holder.itemView.context.getString(R.string.navigation_lbl_rife)
            holder.itemView.item_album_name.text = track.frequency.toString()
            holder.itemView.item_track_options.setOnClickListener {
                mListener?.onTrackOptions(
                    track,
                    position
                )
            }
            holder.itemView.setOnClickListener { mListener?.onTrackClick(track, position) }
        }

        if (position == mData.size - 1) {
            holder.itemView.divider.visibility = View.INVISIBLE
        } else {
            holder.itemView.divider.visibility = View.VISIBLE
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun setData(trackList: List<Any>?) {
        mData = trackList as ArrayList<Any>
        notifyDataSetChanged()
    }

    fun setSelected(selectedPosition: Int) {
        if (selectedPosition > mData.size - 1) {
            return
        }

        mData.forEach {
            if (it is Track) {
                it.isSelected = false
            } else if (it is MusicRepository.Frequency) {
                it.isSelected = false
            }
        }
        mData[selectedPosition].also {
            if (it is Track) {
                it.isSelected = true
            } else if (it is MusicRepository.Frequency) {
                it.isSelected = true
            }
        }
        notifyDataSetChanged()
    }
}