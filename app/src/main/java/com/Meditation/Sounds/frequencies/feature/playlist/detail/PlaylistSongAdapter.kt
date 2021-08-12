package com.Meditation.Sounds.frequencies.feature.playlist.detail

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.models.Album
import com.Meditation.Sounds.frequencies.models.PlaylistItem
import com.Meditation.Sounds.frequencies.models.PlaylistItemSongAndSong
import com.Meditation.Sounds.frequencies.views.CustomPopupDetailProgram
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_playlist_song.view.*


/**
 * Created by Admin on 11/14/16.
 */

class PlaylistSongAdapter(var mContext: Context, private var mPlaylistItem: PlaylistItem,
                          var isFromUser: Boolean, private var mData: List<PlaylistItemSongAndSong>,
                          private var mOnClickListener: IOnItemClickListener? = null)
    : RecyclerView.Adapter<PlaylistSongAdapter.ViewHolder>() {

    var listAlbum = ArrayList<Album>()
    var isDismiss = false
    var isFromFavorites = false
    var mPopup: CustomPopupDetailProgram? = null
    var idSong: Long = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_playlist_song, parent, false))
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mData[position]
//        if (position == mData.size - 1) {
//            holder.itemView.line.visibility = View.GONE
//        } else {
//            holder.itemView.line.visibility = View.VISIBLE
//        }

        if (mData.isNotEmpty()) {
            holder.itemView.line.visibility = View.VISIBLE
        } else {
            holder.itemView.line.visibility = View.GONE
        }
        if (item.song.title.contains("Life Force the Source of Qi")) {
            holder.itemView.item_track_name.text = item.song.title.replace("Life Force the Source of Qi ", "")
        } else {
            holder.itemView.item_track_name.text = item.song.title.replace(item.song.albumName, "").replace(" - ", "")
        }
        holder.itemView.tvAlbumTitle.text = item.song.albumName

        if (listAlbum.size > 0) {
            for (album in listAlbum) {
                if (album.id == item.song.albumId && album.name == item.song.albumName) {
                    if (!TextUtils.isEmpty(album.albumArt)) {
                        if (album.albumArt!!.startsWith("http")) {
                            Glide.with(mContext)
                                    .applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.ic_album_placeholder))
                                    .load(album.albumArt).into(holder.itemView.imgAlbumDetail)
                        } else {
                            Glide.with(mContext)
                                    .applyDefaultRequestOptions(RequestOptions().placeholder(R.drawable.ic_album_placeholder))
                                    .load(album.albumArt).into(holder.itemView.imgAlbumDetail)
                        }
                    } else {
                        holder.itemView.imgAlbumDetail.setBackgroundResource(R.drawable.ic_album_default_small)
                    }
                }
            }
        }

        if (isFromFavorites) {
            if (idSong > -1) {
                if (item.song.id == idSong) {
                    holder.itemView.item_track_name.isSelected = true
                    holder.itemView.tvAlbumTitle.isSelected = true
                } else {
                    holder.itemView.item_track_name.isSelected = false
                    holder.itemView.tvAlbumTitle.isSelected = false
                }
            }
        } else {
            if (mPlaylistItem.isPlaying) {
                holder.itemView.item_track_name.isSelected = true
                holder.itemView.tvAlbumTitle.isSelected = true
                holder.itemView.item_track_name.setTypeface(null, Typeface.BOLD)
            } else {
                holder.itemView.item_track_name.isSelected = false
                holder.itemView.tvAlbumTitle.isSelected = false
                holder.itemView.item_track_name.setTypeface(null, Typeface.NORMAL)
            }
        }

        mPopup = CustomPopupDetailProgram(mContext, isFromUser, mPlaylistItem, item)
        mPopup?.setIOnClickListener(object : CustomPopupDetailProgram.IOnItemClickListener {
            override fun onRemoveFavorite(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
                mOnClickListener?.onRemoveFavorite(playlistItem, song)
                notifyDataSetChanged()
            }

            override fun onMoveDown(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
                mOnClickListener?.onItemTouchListener(true)
//                mOnClickListener?.onMoveItemSong(playlistItem,song,true)
            }

            override fun onMoveUp(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
                mOnClickListener?.onItemTouchListener(false)
//                mOnClickListener?.onMoveItemSong(playlistItem,song,false)
            }

            override fun onItemDeleted(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
                mOnClickListener?.let {
                    it.onItemDeleted(playlistItem, song)
                }
            }

            override fun onItemClick(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
                mOnClickListener?.let {
                    it.onItemClick(playlistItem, song)
                }
            }

            override fun onItemTouchListener() {
            }

            override fun onChangeDuration(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong, isPlus: Boolean) {
                mOnClickListener?.let {
                    it.onChangeDuration(playlistItem, song, isPlus)
                }
            }

            override fun onPlayItemSong(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
                mOnClickListener?.let {
                    it.onPlayItemSong(playlistItem, song)
                }
            }
        })

        if (isDismiss) {
            mPopup?.isOutsideTouchable = true
            mPopup?.dismiss()
        }

        holder.itemView.imgMenu.setOnClickListener {
            if (mPopup!!.isShowing) {
                mPopup?.isOutsideTouchable = true
                mPopup?.dismiss()
            }
            mPopup?.showAsDropDown(holder.itemView.imgMenu)
        }

        holder.itemView.setOnClickListener {
            mOnClickListener?.onPlayItemSong(mPlaylistItem, item)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    fun setDataAlbum(data: ArrayList<Album>) {
        this.listAlbum = data
        notifyDataSetChanged()
    }

    fun setSongPlaying(id: Long) {
        this.idSong = id
        notifyDataSetChanged()
    }

    fun setFromFavorite(isFrom : Boolean){
        this.isFromFavorites = isFrom
        notifyDataSetChanged()
    }

    interface IOnItemClickListener {
        fun onItemDeleted(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong)
        fun onItemClick(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong)
        fun onItemChanged(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong, isVolumeChange: Boolean)
        fun onItemTouchListener(isMoveDown: Boolean)
        fun onChangeDuration(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong, isPlus: Boolean)
        fun onPlayItemSong(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong)
        fun onRemoveFavorite(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong)
        fun onMoveItemSong(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong, isMoveDown: Boolean)
    }
}
