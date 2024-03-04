package com.Meditation.Sounds.frequencies.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.utils.FontUtils


@SuppressLint("CustomViewStyleable")
class CustomFontButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : androidx.appcompat.widget.AppCompatButton(context, attrs, defStyleAttr) {

    private var fontName: String? = null

    init {
        attrs?.apply {
            val a = context.obtainStyledAttributes(this, R.styleable.TextFont)
            fontName = a.getString(R.styleable.TextFont_fontText)
            init()
            try {
                a.recycle()
            } catch (_: Exception) {
            }
        }
    }

    private fun init() {
        if (fontName != null) {
            try {
                typeface = FontUtils.getTypeface(context, this.fontName)
            } catch (_: Exception) {
            }
        }
    }

}