package com.Meditation.Sounds.frequencies.feature.album.detail

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.models.Song
import com.Meditation.Sounds.frequencies.utils.StringsUtils
import com.Meditation.Sounds.frequencies.views.CustomPopupDetailAlbum
import kotlinx.android.synthetic.main.item_album_detail.view.*


/**
 * Created by Admin on 11/14/16.
 */

class AlbumDetailAdapter(var mContext: Context, var data: List<Song>, private var mOnItemClickListener: IOnItemClickListener?) : RecyclerView.Adapter<AlbumDetailAdapter.ViewHolder>() {

    private var idSong: Long = 0
    private var duration: Long = 0
    private var isChange = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_album_detail, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = data[position]

        holder.itemView.tvName.text = song.title
                .replace(song.albumName, "")
                .replace(" - ", "")
                .replace("Life Force the Source of Qi ", "")

        holder.itemView.tvAlbumName.text = song.albumName

        holder.itemView.cbAdded.isSelected = song.added!!

        if (isChange) {
            holder.itemView.tvTime.text = StringsUtils.toString(duration)
        } else {
            holder.itemView.tvTime.text = StringsUtils.toString(song.duration)
        }

        holder.itemView.tvName.isSelected = song.id == idSong
        holder.itemView.tvAlbumName.isSelected = song.id == idSong

        if (position == data.size - 1) {
            holder.itemView.line.visibility = View.GONE
        } else {
            holder.itemView.line.visibility = View.VISIBLE
        }

        val popup = CustomPopupDetailAlbum(mContext, song, duration, object : CustomPopupDetailAlbum.IOnItemClickListener {
            override fun onAddSongToPlaylist(song: Song) {
                mOnItemClickListener?.onAddSongToPlaylist(song)
            }

            override fun onPlusDuration(song: Song, isPlus: Boolean) {
                mOnItemClickListener?.onPlusDuration(song, isPlus)
            }

            override fun onPlaySong(song: Song) {
                mOnItemClickListener?.onPlaySong(song)
            }

            override fun onAddToFavorite(song: Song, isAdd: Int) {
                mOnItemClickListener?.onAddToFavorite(song,isAdd)
            }
        })
        holder.itemView.imgMenu.setOnClickListener {
            if (popup.isShowing){
                popup.isOutsideTouchable = true
                popup.dismiss()
            }
            if (position < data.size - 2) {
                popup.showAsDropDown(holder.itemView.imgMenu)
            } else {
                popup.showAtLocation(holder.itemView.imgMenu, Gravity.BOTTOM, 100, 400)
            }
        }

        holder.itemView.setOnClickListener {
            mOnItemClickListener?.onPlayItemSong(song)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setSongPlaying(id: Long) {
        this.idSong = id
        notifyDataSetChanged()
    }

    fun update(duration: Long, isChange: Boolean) {
        this.duration = duration
        this.isChange = isChange
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    interface IOnItemClickListener {
        fun onAddSongToPlaylist(song: Song)
        fun onPlusDuration(song: Song, isPlus: Boolean)
        fun onPlaySong(song: Song)
        fun onPlayItemSong(song: Song)
        fun onAddToFavorite(song: Song, isAdd: Int)
    }
}
