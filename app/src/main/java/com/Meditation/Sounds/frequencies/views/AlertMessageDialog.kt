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
import kotlinx.android.synthetic.main.dialog_alert_message.*

class AlertMessageDialog(private val mContext: Context?, private var mOnSubmitListener: IOnSubmitListener?) : Dialog(mContext!!) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_alert_message)
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
        btnOK.setOnClickListener {
            if (mOnSubmitListener != null) {
                mOnSubmitListener!!.submit()
            }
            dismiss()
        }
        btnCancel.setOnClickListener {
            if (mOnSubmitListener != null) {
                mOnSubmitListener!!.cancel()
            }
            dismiss()
        }
    }

    fun setWarningMessage(message: String){
        tvDescription.setText(message)
    }

    fun setButtonText(textLeft : String, textRight : String){
        btnCancel.setText(textLeft)
        btnOK.setText(textRight)
    }

    interface IOnSubmitListener {
        fun submit()
        fun cancel()
    }
}
