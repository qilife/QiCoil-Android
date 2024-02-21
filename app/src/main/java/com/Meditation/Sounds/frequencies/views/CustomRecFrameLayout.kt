package com.Meditation.Sounds.frequencies.views

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.R

class CustomRecFrameLayout : FrameLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (Utils.isTablet(context)) {
            var height = heightMeasureSpec
            height = View.MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED)
            setMeasuredDimension((height / 2 - context.resources.getDimension(R.dimen.item_offset)).toInt(), (height / 2 - context.resources.getDimension(R.dimen.item_offset)).toInt())
            if (childCount > 0) {
                val wspec = View.MeasureSpec.makeMeasureSpec(measuredWidth , View.MeasureSpec.EXACTLY)
                val hspec = View.MeasureSpec.makeMeasureSpec(measuredHeight,
                        View.MeasureSpec.EXACTLY)
                for (i in 0 until childCount) {
                    val v = getChildAt(i)
                    v.measure(wspec, hspec)
                }
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}
