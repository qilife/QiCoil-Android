package com.Meditation.Sounds.frequencies.views

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.utils.FontUtils

class CustomFontTextView : TextView {

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

//    override fun setText(text: CharSequence?, type: TextView.BufferType) {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//            super.setText(Html.fromHtml(text?.toString() ?: "", Html.FROM_HTML_MODE_LEGACY), type)
//        } else {
//            super.setText(Html.fromHtml(text?.toString() ?: ""), type)
//        }
//    }
}