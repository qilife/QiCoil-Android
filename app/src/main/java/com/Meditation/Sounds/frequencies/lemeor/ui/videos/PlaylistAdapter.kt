package com.Meditation.Sounds.frequencies.lemeor.ui.videos

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Playlist
import kotlinx.android.synthetic.main.item_category.view.*

class PlaylistAdapter(
        private val mContext: Context,
        private var mData: ArrayList<Playlist>?
) : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    interface Listener {
        fun onClickItem(playlist: Playlist, i: Int)
    }

    private var mListener: Listener? = null

    fun setOnClickListener(listener: Listener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false))
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = mData?.get(position)

        if (playlist?.isSelected!!) {
            holder.itemView.item_name.setTextColor(getColor(mContext, android.R.color.black))
            holder.itemView.item_name.background = getDrawable(mContext, R.drawable.rounded_album_category_selected)
        } else {
            holder.itemView.item_name.setTextColor(getColor(mContext, android.R.color.white))
            holder.itemView.item_name.background = null
        }

        holder.itemView.item_name.text = playlist.name

        holder.itemView.setOnClickListener { mListener?.onClickItem(playlist, position) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun setData(playlist: List<Playlist>) {
        mData = playlist as ArrayList<Playlist>

        if (mData?.isNotEmpty()!!) {
            mData?.get(0)?.isSelected = true
            notifyDataSetChanged()
        }
    }

    fun setSelected(selectedPosition: Int) {
        mData?.forEach { it.isSelected = false }
        mData?.get(selectedPosition)?.isSelected = true
        notifyDataSetChanged()
    }
}