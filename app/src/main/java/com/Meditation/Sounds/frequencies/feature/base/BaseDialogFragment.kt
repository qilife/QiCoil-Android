package com.Meditation.Sounds.frequencies.feature.base

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.Meditation.Sounds.frequencies.R

abstract class BaseDialogFragment : DialogFragment() {
    abstract val layoutId: Int
    open var isCancel = true

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        onObserve()
        return inflater.inflate(layoutId, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    open fun onObserve() {}
    open fun initView() {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(isCancel)
            dialog.setCancelable(isCancel)
        }
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialog?.dismiss()
    }

    override fun onResume() {
        super.onResume()
        dialog?.apply {
            val params = window?.attributes
            params?.width = ViewGroup.LayoutParams.MATCH_PARENT
            params?.height = ViewGroup.LayoutParams.WRAP_CONTENT
            window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.attributes?.apply {
                gravity = Gravity.CENTER
                windowAnimations = R.style.DialogAnimation
            }
            view?.fitsSystemWindows = true
            window?.attributes = params as WindowManager.LayoutParams
        }
    }

//    fun Dialog.setMargin(
//        marginStart: Int = 0,
//        marginTop: Int = 0,
//        marginEnd: Int = 0,
//        marginBottom: Int = 0
//    ) {
//        this.apply {
//            view?.setMarginsInPixels(
//                marginStart.toPx(context).toInt(),
//                marginTop.toPx(context).toInt(),
//                marginEnd.toPx(context).toInt(),
//                marginBottom.toPx(context).toInt()
//            )
//        }
//    }

    fun <T> sendData(data: T, isShow: Int) {
        if (data != null && data is Parcelable) {
            val args = Bundle()
            args.putInt("isShow", isShow)
            args.putParcelable(data!!::class.java.name, data)
            arguments = args
        } else {
            Log.w("sendData", "data is not Parcelable")
        }
    }

    fun showAllowingStateLoss(fm: FragmentManager, tag: String? = null) {
        fm.beginTransaction().add(this, tag).commitAllowingStateLoss()
    }
}