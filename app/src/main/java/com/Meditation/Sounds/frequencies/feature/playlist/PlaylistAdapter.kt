package com.Meditation.Sounds.frequencies.feature.playlist

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.models.Playlist
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.StringsUtils
import kotlinx.android.synthetic.main.item_playlist.view.*

/**
 * Created by Admin on 11/14/16.
 */

class PlaylistAdapter(var data: List<Playlist>, var onItemClickListener: IOnItemClickListener?) : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    var isPurchasedBasic: Boolean = false
    var isPurchasedAdvanced: Boolean = false
    var isPurchasedHigherAbundance: Boolean = false
    var isPurchasedHigherQuantum: Boolean = false

    init {
        initPurchaseRequest()
    }

    fun initPurchaseRequest() {
        isPurchasedBasic = SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)
        isPurchasedAdvanced = SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_ADVANCED)
        isPurchasedHigherAbundance = SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE)
        isPurchasedHigherQuantum = SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_HIGH_QUANTUM)
    }

    fun setListData(list: List<Playlist>) {
        this.data = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_playlist, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = data[position]

        holder.itemView.tvNamePlaylist.text = playlist.title
        holder.itemView.tvTotalTime.text = StringsUtils.toString(playlist.totalTime)
//        if (position == data.size - 1) {
//            holder.itemView.line.visibility = View.GONE
//        } else {
//            holder.itemView.line.visibility = View.VISIBLE
//        }
        holder.itemView.imvLocked.visibility = View.GONE
        if (playlist.fromUsers == 1) {
            if (isUnClocked(playlist.mediaType!!)) {
                holder.itemView.tvDelete.visibility = View.VISIBLE
            } else {
                holder.itemView.tvDelete.visibility = View.INVISIBLE
                holder.itemView.imvLocked.visibility = View.VISIBLE
            }
        } else {
            holder.itemView.tvDelete.visibility = View.INVISIBLE
            if (!isUnClocked(playlist.mediaType!!)) {
                holder.itemView.imvLocked.visibility = View.VISIBLE
            }
        }
    }

    private fun isUnClocked(mediaType: Int): Boolean {
        return when {
            mediaType == Constants.MEDIA_TYPE_ADVANCED -> isPurchasedAdvanced //check advanced playlist
            mediaType < Constants.MEDIA_TYPE_ADVANCED -> {
                //check basic playlist
                if (isPurchasedBasic) {
                    mediaType == Constants.MEDIA_TYPE_BASIC || mediaType == Constants.MEDIA_TYPE_BASIC_FREE
                } else {
                    mediaType == Constants.MEDIA_TYPE_BASIC_FREE
                }
            }
            mediaType == Constants.MEDIA_TYPE_ABUNDANCE -> isPurchasedHigherAbundance
            mediaType == Constants.MEDIA_TYPE_HIGHER_QUANTUM -> isPurchasedHigherQuantum
            else -> true
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(data[layoutPosition], itemView.imvLocked.visibility != View.VISIBLE)
            }
            itemView.tvDelete.setOnClickListener {
                onItemClickListener?.onDeleteItem(data[layoutPosition])
            }
        }
    }

    interface IOnItemClickListener {
        fun onItemClick(playlist: Playlist, isUnlock: Boolean)
        fun onDeleteItem(playlist: Playlist)
    }
}
