package com.Meditation.Sounds.frequencies.feature.playlist.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.db.QFDatabase
import com.Meditation.Sounds.frequencies.models.Album
import com.Meditation.Sounds.frequencies.models.PlaylistItem
import com.Meditation.Sounds.frequencies.models.PlaylistItemSongAndSong
import com.Meditation.Sounds.frequencies.utils.SwipeHelpers.ItemTouchHelperAdapter
import kotlinx.android.synthetic.main.item_playlist_detail.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Admin on 11/14/16.
 */

class PlaylistDetailAdapter(var mContext: Context,
                            var data: ArrayList<PlaylistItem>, var isFromUser: Boolean,
                            var mOnItemClickListener: IOnItemClickListener? = null)//,var mDragStartListener: OnStartDragListener
    : RecyclerView.Adapter<PlaylistDetailAdapter.ViewHolder>(), ItemTouchHelperAdapter {

    var seletectedItems = ArrayList<PlaylistItem>()
        private set

    var listAlbum = ArrayList<Album>()
    var idSong : Long = 0
    var isFromFavorites = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_playlist_detail, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.itemView.isSelected = seletectedItems.contains(item)
        val adapter = PlaylistSongAdapter(mContext, item, isFromUser, item.songs, object : PlaylistSongAdapter.IOnItemClickListener {
            override fun onRemoveFavorite(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
                mOnItemClickListener?.onRemoveFavorite(playlistItem, song)
            }

            override fun onMoveItemSong(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong, isMoveDown: Boolean) {
                if (isMoveDown) {
                    if (position < data.size - 1 && position >= 0) {
                        Collections.swap(data, position, position + 1)
                    }
                } else {
                    if (position > 0 && position <= data.size - 1) {
                        Collections.swap(data, position, position - 1)
                    }
                }
                notifyDataSetChanged()
                mOnItemClickListener?.onMoveFavorite(playlistItem,song)
            }

            override fun onChangeDuration(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong, isPlus: Boolean) {
                mOnItemClickListener?.let {
                    it.onChangeDuration(playlistItem, song, isPlus)
                }
            }

            override fun onItemDeleted(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
                val isContain = seletectedItems.contains(playlistItem)

                item.songs.remove(song)
                if (item.songs.size == 0) {
                    data.remove(playlistItem)
                    seletectedItems.remove(item)

                }

                notifyDataSetChanged()
                if (isContain) mOnItemClickListener?.onSelectedItemChanged(seletectedItems, playlistItem)
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onDeletedItem(playlistItem, song)
                }

            }

            override fun onItemClick(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
            }

            override fun onItemChanged(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong, isVolumeChange: Boolean) {
                mOnItemClickListener?.onItemChanged(playlistItem, song, isVolumeChange)
            }

            override fun onItemTouchListener(isMoveDown: Boolean) {
//                mDragStartListener.onStartDrag(holder)
                if (isMoveDown) {
                    if (position < data.size - 1 && position >= 0) {
                        Collections.swap(data, position, position + 1)
//                        notifyItemMoved(position, position + 1)
                    }
                } else {
                    if (position > 0 && position <= data.size - 1) {
                        Collections.swap(data, position, position - 1)
//                        notifyItemMoved(position, position - 1)
                    }
                }
                notifyDataSetChanged()
            }

            override fun onPlayItemSong(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
                mOnItemClickListener?.let {
                    it.onPlayItemSong(playlistItem, song)
                }
            }
        })
        holder.itemView.rcPlaylist.adapter = adapter
        if (listAlbum.size > 0) {
            adapter.setDataAlbum(listAlbum)
        }
        if (idSong>-1){
            adapter.setSongPlaying(idSong)
        }

        adapter.setFromFavorite(isFromFavorites)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){//, ItemTouchHelperViewHolder
/*        override fun onItemSelected() {

        }

        override fun onItemClear() {
            if (seletectedItems.size > 0) {
                seletectedItems.clear()
            }
            for (i in 0..data.size) {
                if (i >= 0 && i <= data.size) {
                    notifyItemChanged(i)
                }
            }
            mOnItemClickListener?.onAutoSavePlaylist()
        }*/

        init {
            val layoutManager = LinearLayoutManager(itemView.context)
            layoutManager.isAutoMeasureEnabled = true
            itemView.rcPlaylist.layoutManager = layoutManager
        }
    }

    fun updateData(list: ArrayList<PlaylistItem>) {
        data = list
        notifyDataSetChanged()
    }

    fun updateDuration(context: Context, song: PlaylistItemSongAndSong) {
        QFDatabase.getDatabase(context).playlistItemSongDAO().updateEndOffset(
                song.item.id, song.item.endOffset)
        notifyDataSetChanged()
    }

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
        fun onSelectedItemChanged(selectedItems: ArrayList<PlaylistItem>, playlistItem: PlaylistItem)
        fun onDeletedItem(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong)
        fun onItemChanged(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong, isVolumeChange: Boolean)
        fun onAutoSavePlaylist()
        fun onChangeDuration(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong, isPlus: Boolean)
        fun onPlayItemSong(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong)
        fun onRemoveFavorite(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong)
        fun onMoveFavorite(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (data.size > 0 && fromPosition >= 0 && toPosition >= 0) {
            Collections.swap(data, fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
        }
        return true
    }

    override fun onItemDismiss(position: Int) {

    }
}
