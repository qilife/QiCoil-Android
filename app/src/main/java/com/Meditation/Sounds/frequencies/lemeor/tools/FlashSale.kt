package com.Meditation.Sounds.frequencies.lemeor.tools

import android.content.Context
import android.os.CountDownTimer
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.flashSaleRemain
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.flashSaleTimeStamp
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.preference

object FlashSale {

    interface FlashSaleMainInterface {
        fun onFlashSaleTick(hours: String, minutes: String, seconds: String)
        fun onFlashSaleFinish()
    }

    interface FlashSaleOptionsInterface {
        fun onFlashSaleTick(hours: String, minutes: String, seconds: String)
        fun onFlashSaleFinish()
    }

    interface FlashSaleScreenInterface {
        fun onFlashSaleTick(hours: String, minutes: String, seconds: String)
        fun onFlashSaleFinish()
    }

    var fScreenInterface: FlashSaleScreenInterface? = null
    var fsOptionsInterface: FlashSaleOptionsInterface? = null
    var fsMainInterface: FlashSaleMainInterface? = null

    var flashSaleTimer: CountDownTimer? = null
    var remain: Long = 0

    fun setFSScreenInterface(anInterface: FlashSaleScreenInterface) {
        fScreenInterface = anInterface
    }

    fun setFSOptionsInterface(anInterface: FlashSaleOptionsInterface) {
        fsOptionsInterface = anInterface
    }

    fun setFSMainInterface(anInterface: FlashSaleMainInterface) {
        fsMainInterface = anInterface
    }

    fun startFlashSale(time: Long) {
        if (time > 0) {
            flashSaleTimer = object : CountDownTimer(time, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    remain = millisUntilFinished

                    val seconds: Long = millisUntilFinished / 1000 % 60
                    val minutes: Long = millisUntilFinished / (1000 * 60) % 60
                    val hours: Long = millisUntilFinished / (1000 * 60 * 60) % 24

                    fsMainInterface?.onFlashSaleTick(checkForZero(hours), checkForZero(minutes), checkForZero(seconds))
                    fsOptionsInterface?.onFlashSaleTick(checkForZero(hours), checkForZero(minutes), checkForZero(seconds))
                    fScreenInterface?.onFlashSaleTick(checkForZero(hours), checkForZero(minutes), checkForZero(seconds))
                }

                override fun onFinish() {
                    fsMainInterface?.onFlashSaleFinish()
                    fsOptionsInterface?.onFlashSaleFinish()
                    fScreenInterface?.onFlashSaleFinish()
                }
            }.start()
        } else {
            fsMainInterface?.onFlashSaleFinish()
            fsOptionsInterface?.onFlashSaleFinish()
            fScreenInterface?.onFlashSaleFinish()
        }
    }

    private fun checkForZero(time: Long) : String {
        return if (time.toString().length == 1) { "0$time" } else { time.toString() }
    }

    fun saveFlashSaleTime(context: Context) {
        preference(context).flashSaleRemain = remain
        preference(context).flashSaleTimeStamp = System.currentTimeMillis()
    }
}