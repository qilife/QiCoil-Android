package com.Meditation.Sounds.frequencies.views

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.models.PlaylistItem
import com.Meditation.Sounds.frequencies.models.PlaylistItemSongAndSong
import com.Meditation.Sounds.frequencies.utils.StringsUtils

class CustomPopupDetailProgram(var mContext: Context, var isFromUser: Boolean,
                               var playlistItem: PlaylistItem, var song: PlaylistItemSongAndSong)
    : PopupWindow() {//, var mListener: IOnItemClickListener

    private val layoutInflater = LayoutInflater.from(mContext)
    private var view: View? = null
    var mListener: IOnItemClickListener? = null

    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (view != null) {
                initComponent(view!!)
            }
        }
    }

    private var broadcastDismissReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (isShowing) {
                isOutsideTouchable = true
                dismiss()
            }
        }
    }

    init {
        contentView = layoutInflater.inflate(R.layout.popup_detail_playlist, null)
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        view = contentView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext.registerReceiver(
                broadcastReceiver,
                IntentFilter("BROADCAST_CHANGE_DURATION_PROGRAM"),
                Context.RECEIVER_EXPORTED
            )
            mContext.registerReceiver(
                broadcastDismissReceiver,
                IntentFilter("BROADCAST_DISMISS_POPUP_PROGRAM"),
                Context.RECEIVER_EXPORTED
            )
        } else {
            mContext.registerReceiver(
                broadcastReceiver,
                IntentFilter("BROADCAST_CHANGE_DURATION_PROGRAM")
            )
            mContext.registerReceiver(
                broadcastDismissReceiver,
                IntentFilter("BROADCAST_DISMISS_POPUP_PROGRAM")
            )
        }
        initComponent(contentView)
    }

    private fun initComponent(view: View) {
        val btnPlay = view.findViewById<CustomFontTextView>(R.id.btnPlay)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnPlus = view.findViewById<ImageView>(R.id.btnPlus)
        val btnMoveUp = view.findViewById<CustomFontTextView>(R.id.btnMoveUp)
        val btnMoveDown = view.findViewById<CustomFontTextView>(R.id.btnMoveDown)
        val btnDelete = view.findViewById<CustomFontTextView>(R.id.btnDelete)
        val btnMinus = view.findViewById<ImageView>(R.id.btnMinus)
        val tvDuration = view.findViewById<CustomFontTextView>(R.id.tvDuration)
        val btnRemoveFavorite = view.findViewById<CustomFontTextView>(R.id.btnRemoteFavorite)

        if (song.song.favorite == 1){
            btnRemoveFavorite.visibility = View.GONE
        }else{
            btnRemoveFavorite.visibility = View.GONE
        }

        btnRemoveFavorite.setOnClickListener {
            mListener?.onRemoveFavorite(playlistItem,song)
            dismiss()
        }

        btnMoveDown.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                mListener?.onMoveDown(playlistItem, song)
                dismiss()
                return false
            }
        })
        btnMoveUp.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                mListener?.onMoveUp(playlistItem, song)
                dismiss()
                return false
            }
        })

        if (isFromUser) {
            btnDelete.visibility = View.VISIBLE
        } else {
            btnDelete.visibility = View.GONE
        }
        btnDelete.setOnClickListener {
            mListener?.let {
                it.onItemDeleted(playlistItem, song)
                dismiss()
            }
        }

        tvDuration.text = StringsUtils.toString(song.item.endOffset)
        btnPlus.setOnClickListener {
            mListener?.let {
                it.onChangeDuration(playlistItem, song, true)
            }
        }
        btnMinus.setOnClickListener {
            mListener?.let {
                it.onChangeDuration(playlistItem, song, false)
            }
        }

        btnPlay.setOnClickListener {
            mListener?.let {
                it.onPlayItemSong(playlistItem, song)
                dismiss()
            }
        }

        btnBack.setOnClickListener {
            if (isShowing) {
                isOutsideTouchable = true
                dismiss()
            }
        }
    }

    fun show(anchorView: View) {
        showAsDropDown(anchorView)
    }

    fun setIOnClickListener(listener: IOnItemClickListener) {
        this.mListener = listener
    }

    interface IOnItemClickListener {
        fun onItemDeleted(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong)
        fun onItemClick(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong)
        fun onItemTouchListener()
        fun onChangeDuration(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong, isPlus: Boolean)
        fun onMoveUp(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong)
        fun onMoveDown(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong)
        fun onPlayItemSong(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong)
        fun onRemoveFavorite(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong)
    }
}
