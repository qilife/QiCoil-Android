package com.Meditation.Sounds.frequencies.lemeor.tools

import android.app.Activity
import androidx.core.content.ContextCompat.getColor
import com.kaopiz.kprogresshud.KProgressHUD

object HudHelper {

    private var hud: KProgressHUD? = null

    fun show(activity: Activity) {
        hud = KProgressHUD.create(activity)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setDimAmount(0.7f)
            .setBackgroundColor(getColor(activity.applicationContext, android.R.color.transparent))
        hud?.show()
    }

    fun hide() { hud?.dismiss() }
}