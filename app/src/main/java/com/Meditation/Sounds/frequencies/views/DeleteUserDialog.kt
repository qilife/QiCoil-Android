package com.Meditation.Sounds.frequencies.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import com.Meditation.Sounds.frequencies.R
import kotlinx.android.synthetic.main.dialog_delete_user.*
import kotlinx.android.synthetic.main.dialog_delete_user.btnSubmit

class DeleteUserDialog(private val mContext: Context?, private var mOnSubmitListener: IOnSubmitListener?) : Dialog(mContext!!) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_delete_user)
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
        btnCancel.setOnClickListener { dismiss() }

        btnSubmit.setOnClickListener {
            dismiss()
            mOnSubmitListener?.submit(edtPassword.text.toString())
        }
    }

    interface IOnSubmitListener {
        fun submit(password: String)
    }
}