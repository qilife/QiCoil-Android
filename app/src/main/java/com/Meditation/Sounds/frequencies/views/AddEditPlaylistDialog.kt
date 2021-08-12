package com.Meditation.Sounds.frequencies.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.models.Playlist
import kotlinx.android.synthetic.main.dialog_add_edit_playlist.*

class AddEditPlaylistDialog(private val mContext: Context?, private val mPlaylist: Playlist?, private var mOnSubmitListener: IOnSubmitListener?) : Dialog(mContext!!) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_add_edit_playlist)
        val window = this.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.CENTER
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window.attributes = wlp
        getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window!!.attributes = wlp
        init()
    }

    fun init() {
        if (mPlaylist != null) {
            description.text = mContext!!.getString(R.string.txt_edit_playlist)
            edtPlayListName.setText(mPlaylist.title)
            edtPlayListName.setSelection(edtPlayListName.text.length)
        } else {
            description.text = mContext!!.getString(R.string.txt_create_playlist)
        }

        btnSubmit.setOnClickListener {
            if (edtPlayListName.text.isEmpty()) {
                Toast.makeText(mContext, mContext!!.getString(R.string.txt_plz_enter_playlist_name), Toast.LENGTH_SHORT).show()
            } else {
                dismiss()
                mOnSubmitListener?.submit(edtPlayListName.text.toString())
            }
        }
    }


    interface IOnSubmitListener {
        fun submit(name: String)
    }
}
