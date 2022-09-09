package com.Meditation.Sounds.frequencies.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.Meditation.Sounds.frequencies.R
import kotlinx.android.synthetic.main.dialog_disclaimer.*

class DisclaimerDialog(private val mContext: Context?, var isShowCheckbox: Boolean, private var mOnSubmitListener: IOnSubmitListener?) : Dialog(mContext!!) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_disclaimer)
        val window = this.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.CENTER
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window.attributes = wlp
        getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window!!.attributes = wlp
        setCancelable(false)
        init()
    }

    fun init() {
        if(isShowCheckbox){
            viewCheckbox.visibility = View.VISIBLE
        } else {
            viewCheckbox.visibility = View.INVISIBLE
        }

        btnOK.setOnClickListener {
            if (mOnSubmitListener != null) {
                mOnSubmitListener!!.submit(cbAgain.isChecked)
            }
            dismiss()
        }
        tvAgain.setOnClickListener {
            cbAgain.isChecked = !cbAgain.isChecked
        }
    }

    fun setButtonText(text : String){
        btnOK.text = text
    }

    interface IOnSubmitListener {
        fun submit(isCheck: Boolean)
    }
}
