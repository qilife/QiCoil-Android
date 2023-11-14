package com.Meditation.Sounds.frequencies.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.AttrRes
import android.util.AttributeSet
import android.view.View

class RoundedView :View{

    var roundedBackgroundColor: Int = 0
    set(color: Int){
        field = color
        mPain.color = color
        invalidate()
    }
    private val mPain = Paint()


    constructor(context: Context) : super(context, null) {

    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

    }

    init {
        mPain.color = roundedBackgroundColor
        mPain.isAntiAlias = true
        mPain.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawCircle(width/2f, height/2f, Math.min(width, height)/2f, mPain)
    }
}
