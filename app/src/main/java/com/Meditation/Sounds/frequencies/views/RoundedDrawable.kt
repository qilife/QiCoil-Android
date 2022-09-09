package com.Meditation.Sounds.frequencies.views


import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.widget.ImageView
import android.widget.ImageView.ScaleType

class RoundedDrawable internal constructor(bitmap: Bitmap) : Drawable() {

    private val mBounds = RectF()
    private val mDrawableRect = RectF()
    private val mBitmapRect = RectF()
    private val mBitmapShader: BitmapShader
    private val mBitmapPaint: Paint
    private val mBitmapWidth: Int
    private val mBitmapHeight: Int
    private val mBorderRect = RectF()
    private val mBorderPaint: Paint
    private val mShaderMatrix = Matrix()


    private var mCornerRadius = 0f
    private var mOval = false
    var borderWidth = 0f
        private set
    private var mBorderColor = ColorStateList.valueOf(DEFAULT_BORDER_COLOR)
    private var mScaleType = ScaleType.FIT_XY

    val borderColor: Int
        get() = mBorderColor.defaultColor

    init {

        mBitmapWidth = bitmap.width
        mBitmapHeight = bitmap.height
        mBitmapRect.set(0f, 0f, mBitmapWidth.toFloat(), mBitmapHeight.toFloat())

        mBitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        mBitmapShader.setLocalMatrix(mShaderMatrix)

        mBitmapPaint = Paint()
        mBitmapPaint.style = Paint.Style.FILL
        mBitmapPaint.isAntiAlias = true
        mBitmapPaint.shader = mBitmapShader

        mBorderPaint = Paint()
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.isAntiAlias = true
        mBorderPaint.color = mBorderColor.getColorForState(state, DEFAULT_BORDER_COLOR)
        mBorderPaint.strokeWidth = borderWidth
    }

    override fun isStateful(): Boolean {
        return mBorderColor.isStateful
    }

    override fun onStateChange(state: IntArray): Boolean {
        val newColor = mBorderColor.getColorForState(state, 0)
        if (mBorderPaint.color != newColor) {
            mBorderPaint.color = newColor
            return true
        } else {
            return super.onStateChange(state)
        }
    }

    private fun updateShaderMatrix() {
        mBorderRect.set(mBounds)
        mDrawableRect.set(borderWidth, borderWidth, mBorderRect.width() - borderWidth, mBorderRect.height() - borderWidth)

        val scale: Float
        var dx: Float
        var dy: Float

        when (mScaleType) {
            ImageView.ScaleType.CENTER -> {
                mBorderRect.set(mBounds)
                mDrawableRect.set(borderWidth, borderWidth, mBorderRect.width() - borderWidth, mBorderRect.height() - borderWidth)

                mShaderMatrix.set(null)
                mShaderMatrix.setTranslate(((mDrawableRect.width() - mBitmapWidth) * 0.5f + 0.5f).toInt().toFloat(), ((mDrawableRect.height() - mBitmapHeight) * 0.5f + 0.5f).toInt().toFloat())
            }
            ImageView.ScaleType.CENTER_CROP -> {
                mBorderRect.set(mBounds)
                mDrawableRect.set(borderWidth, borderWidth, mBorderRect.width() - borderWidth, mBorderRect.height() - borderWidth)

                mShaderMatrix.set(null)

                dx = 0f
                dy = 0f

                if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
                    scale = mDrawableRect.height() / mBitmapHeight.toFloat()
                    dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f
                } else {
                    scale = mDrawableRect.width() / mBitmapWidth.toFloat()
                    dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f
                }

                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate((dx + 0.5f).toInt() + borderWidth, (dy + 0.5f).toInt() + borderWidth)
            }
            ImageView.ScaleType.CENTER_INSIDE -> {
                mShaderMatrix.set(null)

                if (mBitmapWidth <= mBounds.width() && mBitmapHeight <= mBounds.height()) {
                    scale = 1.0f
                } else {
                    scale = Math.min(mBounds.width() / mBitmapWidth.toFloat(),
                            mBounds.height() / mBitmapHeight.toFloat())
                }

                dx = ((mBounds.width() - mBitmapWidth * scale) * 0.5f + 0.5f).toInt().toFloat()
                dy = ((mBounds.height() - mBitmapHeight * scale) * 0.5f + 0.5f).toInt().toFloat()

                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate(dx, dy)

                mBorderRect.set(mBitmapRect)
                mShaderMatrix.mapRect(mBorderRect)
                mDrawableRect.set(mBorderRect.left + borderWidth, mBorderRect.top + borderWidth, mBorderRect.right - borderWidth, mBorderRect.bottom - borderWidth)
                mShaderMatrix.setRectToRect(mBitmapRect, mDrawableRect, Matrix.ScaleToFit.FILL)
            }
            ImageView.ScaleType.FIT_CENTER -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mDrawableRect.set(mBorderRect.left + borderWidth, mBorderRect.top + borderWidth, mBorderRect.right - borderWidth, mBorderRect.bottom - borderWidth)
                mShaderMatrix.setRectToRect(mBitmapRect, mDrawableRect, Matrix.ScaleToFit.FILL)
            }
            ImageView.ScaleType.FIT_END -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.END)
                mShaderMatrix.mapRect(mBorderRect)
                mDrawableRect.set(mBorderRect.left + borderWidth, mBorderRect.top + borderWidth, mBorderRect.right - borderWidth, mBorderRect.bottom - borderWidth)
                mShaderMatrix.setRectToRect(mBitmapRect, mDrawableRect, Matrix.ScaleToFit.FILL)
            }
            ImageView.ScaleType.FIT_START -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.START)
                mShaderMatrix.mapRect(mBorderRect)
                mDrawableRect.set(mBorderRect.left + borderWidth, mBorderRect.top + borderWidth, mBorderRect.right - borderWidth, mBorderRect.bottom - borderWidth)
                mShaderMatrix.setRectToRect(mBitmapRect, mDrawableRect, Matrix.ScaleToFit.FILL)
            }
            ImageView.ScaleType.FIT_XY -> {
                mBorderRect.set(mBounds)
                mDrawableRect.set(0 + borderWidth, 0 + borderWidth, mBorderRect.width() - borderWidth, mBorderRect.height() - borderWidth)
                mShaderMatrix.set(null)
                mShaderMatrix.setRectToRect(mBitmapRect, mDrawableRect, Matrix.ScaleToFit.FILL)
            }
            else -> {
                mBorderRect.set(mBounds)
                mDrawableRect.set(0 + borderWidth, 0 + borderWidth, mBorderRect.width() - borderWidth, mBorderRect.height() - borderWidth)
                mShaderMatrix.set(null)
                mShaderMatrix.setRectToRect(mBitmapRect, mDrawableRect, Matrix.ScaleToFit.FILL)
            }
        }

        mBorderRect.inset(borderWidth / 2, borderWidth / 2)

        mBitmapShader.setLocalMatrix(mShaderMatrix)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        mBounds.set(bounds)

        updateShaderMatrix()
    }

    override fun draw(canvas: Canvas) {

        if (mOval) {
            if (borderWidth > 0) {
                canvas.drawOval(mBorderRect, mBorderPaint)
                canvas.drawOval(mDrawableRect, mBitmapPaint)
            } else {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
            }
        } else {
            if (borderWidth > 0) {
                canvas.drawRoundRect(mBorderRect, mCornerRadius, mCornerRadius, mBorderPaint)
                canvas.drawRoundRect(mDrawableRect, Math.max(mCornerRadius - borderWidth, 0f), Math.max(mCornerRadius - borderWidth, 0f), mBitmapPaint)
            } else {
                canvas.drawRoundRect(mDrawableRect, mCornerRadius, mCornerRadius, mBitmapPaint)
            }
        }
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setAlpha(alpha: Int) {
        mBitmapPaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mBitmapPaint.colorFilter = cf
        invalidateSelf()
    }

    override fun getIntrinsicWidth(): Int {
        return mBitmapWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mBitmapHeight
    }

    fun getCornerRadius(): Float {
        return mCornerRadius
    }

    fun setCornerRadius(radius: Float): RoundedDrawable {
        mCornerRadius = radius
        return this
    }

    fun setBorderWidth(width: Int): RoundedDrawable {
        borderWidth = width.toFloat()
        mBorderPaint.strokeWidth = borderWidth
        return this
    }

    fun setBorderColor(color: Int): RoundedDrawable {
        return setBorderColors(ColorStateList.valueOf(color))
    }

    fun getBorderColors(): ColorStateList {
        return mBorderColor
    }

    fun setBorderColors(colors: ColorStateList?): RoundedDrawable {
        mBorderColor = colors ?: ColorStateList.valueOf(0)
        mBorderPaint.color = mBorderColor.getColorForState(state, DEFAULT_BORDER_COLOR)
        return this
    }

    fun isOval(mOval: Boolean): Boolean {
        return this.mOval
    }

    fun setOval(oval: Boolean): RoundedDrawable {
        mOval = oval
        return this
    }

    fun getScaleType(): ScaleType {
        return mScaleType
    }

    fun setScaleType(scaleType: ScaleType?): RoundedDrawable {
        var scaleType = scaleType
        if (scaleType == null) {
            scaleType = ScaleType.FIT_XY
        }
        if (mScaleType != scaleType) {
            mScaleType = scaleType
            updateShaderMatrix()
        }
        return this
    }

    companion object {

        val TAG = "RoundedDrawable"
        val DEFAULT_BORDER_COLOR = Color.BLACK

        fun drawableToBitmap(drawable: Drawable): Bitmap? {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            val bitmap: Bitmap?
            val width = drawable.intrinsicWidth
            val height = drawable.intrinsicHeight
            if (width > 0 && height > 0) {
                bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888)
                val canvas = Canvas(bitmap!!)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            } else {
                bitmap = null
            }

            return bitmap
        }

        fun fromDrawable(drawable: Drawable?): Drawable? {
            if (drawable != null) {
                if (drawable is RoundedDrawable) {
                    // just return if it's already a RoundedDrawable
                    return drawable
                } else if (drawable is ColorDrawable) {
                    // we don't support ColorDrawables yet
                    return drawable
                } else if (drawable is LayerDrawable) {
                    val ld = drawable as LayerDrawable?
                    val num = ld!!.numberOfLayers

                    // loop through layers to and change to RoundedDrawables if possible
                    for (i in 0 until num) {
                        val d = ld.getDrawable(i)
                        ld.setDrawableByLayerId(ld.getId(i), fromDrawable(d))
                    }
                    return ld
                }

                // try to get a bitmap from the drawable and
                val bm = drawableToBitmap(drawable)
                if (bm != null) {
                    return RoundedDrawable(bm)
                } else {
                    Log.w(TAG, "Failed to create bitmap from drawable!")
                }
            }
            return drawable
        }
    }
}
