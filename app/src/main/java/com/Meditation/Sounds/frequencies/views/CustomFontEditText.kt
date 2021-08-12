package com.Meditation.Sounds.frequencies.views

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.utils.FontUtils

class CustomFontEditText(context: Context, attrs: AttributeSet) : EditText(context, attrs) {


    internal var fontName: String? = null

    init {
        val a = context.obtainStyledAttributes(attrs,
                R.styleable.TextFont, 0, 0)
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