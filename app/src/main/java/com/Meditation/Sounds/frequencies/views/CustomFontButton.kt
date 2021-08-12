package com.Meditation.Sounds.frequencies.views

import android.content.Context
import android.util.AttributeSet
import android.widget.Button

import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.utils.FontUtils


class CustomFontButton : Button {

    internal var fontName: String? = null

    constructor(context: Context) : super(context) {}

    @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyle: Int = 0) : super(context, attrs, defStyle) {
        val a = context.obtainStyledAttributes(attrs,
                R.styleable.TextFont, defStyle, 0)
        fontName = a.getString(R.styleable.TextFont_fontText)
        init()
        try {
            a.recycle()
        } catch (ex: Exception) {
        }

    }

    private fun init() {
        if (fontName != null) {
            try {
                typeface = FontUtils.getTypeface(context, this.fontName)
            } catch (e: Exception) {
            }

        }
    }

}