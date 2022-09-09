package com.Meditation.Sounds.frequencies.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView

import com.Meditation.Sounds.frequencies.R


class RoundedConnerImageView : ImageView {
    protected var mCornerRadius = DEFAULT_RADIUS
    protected var mBorderWidth = DEFAULT_BORDER_WIDTH
    protected var mBorderColor: ColorStateList? = ColorStateList
            .valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
    protected var mRoundBackground = false
    protected var mOval = false
    protected var mDrawable: Drawable? = null
    protected var mBackgroundDrawable: Drawable? = null
    protected var mScaleType: ScaleType? = null

    var cornerRadius: Int
        get() = mCornerRadius
        set(radius) {
            if (mCornerRadius == radius) {
                return
            }

            mCornerRadius = radius
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs()
        }

    var borderWidth: Int
        get() = mBorderWidth
        set(width) {
            if (mBorderWidth == width) {
                return
            }

            mBorderWidth = width
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs()
            invalidate()
        }

    var borderColor: Int
        get() = mBorderColor!!.defaultColor
        set(color) {
            borderColors = ColorStateList.valueOf(color)
        }

    var borderColors: ColorStateList?
        get() = mBorderColor
        set(colors) {
            if (mBorderColor == colors) {
                return
            }

            mBorderColor = colors ?: ColorStateList
                    .valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs()
            if (mBorderWidth > 0) {
                invalidate()
            }
        }

    var isOval: Boolean
        get() = mOval
        set(oval) {
            mOval = oval
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs()
            invalidate()
        }

    var isRoundBackground: Boolean
        get() = mRoundBackground
        set(roundBackground) {
            if (mRoundBackground == roundBackground) {
                return
            }

            mRoundBackground = roundBackground
            updateBackgroundDrawableAttrs()
            invalidate()
        }

    constructor(context: Context) : super(context) {}

    @JvmOverloads constructor(context: Context, attrs: AttributeSet,
                              defStyle: Int = 0) : super(context, attrs, defStyle) {

        val a = context.obtainStyledAttributes(attrs,
                R.styleable.RoundedConnerImageView, defStyle, 0)

        val index = a.getInt(
                R.styleable.RoundedConnerImageView_android_scaleType, -1)
        if (index >= 0) {
            scaleType = sScaleTypeArray[index]
        }

        mCornerRadius = a.getDimensionPixelSize(
                R.styleable.RoundedConnerImageView_corner_radius, -1)
        mBorderWidth = a.getDimensionPixelSize(
                R.styleable.RoundedConnerImageView_border_width, -1)

        // don't allow negative values for radius and border
        if (mCornerRadius < 0) {
            mCornerRadius = DEFAULT_RADIUS
        }
        if (mBorderWidth < 0) {
            mBorderWidth = DEFAULT_BORDER_WIDTH
        }

        mBorderColor = a
                .getColorStateList(R.styleable.RoundedConnerImageView_border_color)
        if (mBorderColor == null) {
            mBorderColor = ColorStateList
                    .valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
        }

        mRoundBackground = a.getBoolean(
                R.styleable.RoundedConnerImageView_round_background, false)
        mOval = a.getBoolean(R.styleable.RoundedConnerImageView_is_oval, false)

        updateDrawableAttrs()
        updateBackgroundDrawableAttrs()

        a.recycle()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        invalidate()
    }

    /**
     * Return the current scale type in use by this ImageView.
     *
     * @attr ref android.R.styleable#ImageView_scaleType
     * @see ScaleType
     */
    override fun getScaleType(): ImageView.ScaleType? {
        return mScaleType
    }

    /**
     * Controls how the image should be resized or moved to match the size of
     * this ImageView.
     *
     * @param scaleType
     * The desired scaling mode.
     * @attr ref android.R.styleable#ImageView_scaleType
     */

    override fun setScaleType(scaleType: ImageView.ScaleType?) {
        if (scaleType == null) {
            throw NullPointerException()
        }

        if (mScaleType != scaleType) {
            mScaleType = scaleType

            when (scaleType) {
                ImageView.ScaleType.CENTER, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_INSIDE, ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.FIT_START, ImageView.ScaleType.FIT_END, ImageView.ScaleType.FIT_XY -> super.setScaleType(ImageView.ScaleType.FIT_XY)
                else -> super.setScaleType(scaleType)
            }

            updateDrawableAttrs()
            updateBackgroundDrawableAttrs()
            invalidate()
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        if (drawable != null) {
            mDrawable = RoundedDrawable.fromDrawable(drawable)
            updateDrawableAttrs()
        } else {
            mDrawable = null
        }
        super.setImageDrawable(mDrawable)
    }

    override fun setImageBitmap(bm: Bitmap?) {
        if (bm != null) {
            mDrawable = RoundedDrawable(bm)
            updateDrawableAttrs()
        } else {
            mDrawable = null
        }
        super.setImageDrawable(mDrawable)
    }

    override fun setBackground(background: Drawable) {
        setBackgroundDrawable(background)
    }

    protected fun updateDrawableAttrs() {
        updateAttrs(mDrawable, false)
    }

    protected fun updateBackgroundDrawableAttrs() {
        updateAttrs(mBackgroundDrawable, true)
    }

    protected fun updateAttrs(drawable: Drawable?, background: Boolean) {
        if (drawable == null) {
            return
        }

        if (drawable is RoundedDrawable) {
            drawable
                    .setScaleType(mScaleType)
                    .setCornerRadius(
                            (if (!mRoundBackground && background) 0 else mCornerRadius).toFloat())
                    .setBorderWidth(
                            if (!mRoundBackground && background) 0 else mBorderWidth)
                    .setBorderColors(mBorderColor).isOval(mOval)
        } else if (drawable is LayerDrawable) {
            // loop through layers to and set drawable attrs
            val ld = drawable as LayerDrawable?
            val layers = ld!!.getNumberOfLayers()
            for (i in 0 until layers) {
                updateAttrs(ld.getDrawable(i), background)
            }
        }
    }

    @Deprecated("")
    override fun setBackgroundDrawable(background: Drawable) {
        mBackgroundDrawable = RoundedDrawable.fromDrawable(background)
        updateBackgroundDrawableAttrs()
        super.setBackgroundDrawable(mBackgroundDrawable)
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        setImageDrawable(drawable)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(heightMeasureSpec, View.MeasureSpec.UNSPECIFIED)
        setMeasuredDimension((heightMeasureSpec / 2 - context.resources.getDimension(R.dimen.item_offset)).toInt(), (heightMeasureSpec / 2 - context.resources.getDimension(R.dimen.item_offset)).toInt())
    }

    companion object {

        val TAG = "RoundedImageView"
        val DEFAULT_RADIUS = 0
        val DEFAULT_BORDER_WIDTH = 0
        protected val sScaleTypeArray = arrayOf(ImageView.ScaleType.MATRIX, ImageView.ScaleType.FIT_XY, ImageView.ScaleType.FIT_START, ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.FIT_END, ImageView.ScaleType.CENTER, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_INSIDE)
    }
}
