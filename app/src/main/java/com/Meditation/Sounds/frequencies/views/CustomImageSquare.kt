package com.Meditation.Sounds.frequencies.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

/**
 * Created by DC-MEN on 8/18/2018.
 */
class CustomImageSquare : ImageView {
    constructor(context: Context):super(context){}
    constructor(context: Context, attrs: AttributeSet):super(context, attrs){}
    constructor(context: Context, attrs: AttributeSet, defStyle: Int):super(context, attrs, defStyle){}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(heightMeasureSpec/2, heightMeasureSpec/2)
    }
}