package com.Meditation.Sounds.frequencies.views

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.models.Song
import com.Meditation.Sounds.frequencies.utils.StringsUtils

@SuppressLint("InflateParams")
class CustomPopupDetailAlbum(var mContext: Context, var song: Song,
                             var duration: Long,
                             var mListener: IOnItemClickListener) : PopupWindow() {

    private val layoutInflater = LayoutInflater.from(mContext)
    private var view: View? = null

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
        contentView = layoutInflater.inflate(R.layout.popup_detail_album, null)
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        view = contentView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext.registerReceiver(broadcastReceiver, IntentFilter("BROADCAST_RELOAD_POPUP_ALBUM"), Context.RECEIVER_EXPORTED)
            mContext.registerReceiver(broadcastDismissReceiver, IntentFilter("BROADCAST_DISMISS_POPUP_PROGRAM"), Context.RECEIVER_EXPORTED)
        }else{
            mContext.registerReceiver(broadcastReceiver, IntentFilter("BROADCAST_RELOAD_POPUP_ALBUM"))
            mContext.registerReceiver(broadcastDismissReceiver, IntentFilter("BROADCAST_DISMISS_POPUP_PROGRAM"))
        }
        initComponent(contentView)
    }

    private fun initComponent(view: View) {
        val btnPlay = view.findViewById<CustomFontTextView>(R.id.btnPlay)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnAddToProgram = view.findViewById<CustomFontTextView>(R.id.btnAddToProgram)
        val btnAddToFavorite = view.findViewById<CustomFontTextView>(R.id.btnAddToFavorites)
        val btnPlus = view.findViewById<ImageView>(R.id.btnPlus)
        val btnMinus = view.findViewById<ImageView>(R.id.btnMinus)
        val tvDuration = view.findViewById<CustomFontTextView>(R.id.tvDuration)

        if (song.favorite == 1){
            btnAddToFavorite.text = "Remove from Favorite"
        }else{
            btnAddToFavorite.text = mContext.getString(R.string.tv_add_to_favorites)
        }

        tvDuration.text = StringsUtils.toString(song.duration)
        btnAddToProgram.setOnClickListener {
            mListener.let {
                it.onAddSongToPlaylist(song)
                dismiss()
            }
        }

        btnAddToFavorite.setOnClickListener {

            mListener.let {
                if (btnAddToFavorite.text == mContext.getString(R.string.tv_add_to_favorites)){
                    it.onAddToFavorite(song,1)
                }else{
                    it.onAddToFavorite(song,0)
                }
                dismiss()
            }
        }
        btnPlay.setOnClickListener {
            mListener.let {
                it.onPlaySong(song)
                dismiss()
            }
        }
        btnPlus.setOnClickListener {
            mListener.let {
                it.onPlusDuration(song, true)
            }
        }
        btnMinus.setOnClickListener {
            mListener.let {
                it.onPlusDuration(song, false)
            }
        }

        btnBack.setOnClickListener {
            if (isShowing){
                isOutsideTouchable = true
                dismiss()
            }
        }
    }

    fun show(anchorView: View) {
        showAsDropDown(anchorView)
    }

    interface IOnItemClickListener {
        fun onAddSongToPlaylist(song: Song)
        fun onPlusDuration(song: Song, isPlus: Boolean)
        fun onPlaySong(song: Song)
        fun onAddToFavorite(song: Song, isAdd: Int)
    }
}
