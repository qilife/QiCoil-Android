package com.Meditation.Sounds.frequencies.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.github.siyamed.shapeimageview.RoundedImageView

import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.utils.Utils

class RecImageView : RoundedImageView {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var height = heightMeasureSpec
        if(Utils.isTablet(context)){
            var height = heightMeasureSpec
            height = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.UNSPECIFIED)
            setMeasuredDimension((height / 2 - context.resources.getDimension(R.dimen.item_offset)).toInt(), (height / 2 - context.resources.getDimension(R.dimen.item_offset)).toInt())
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}
