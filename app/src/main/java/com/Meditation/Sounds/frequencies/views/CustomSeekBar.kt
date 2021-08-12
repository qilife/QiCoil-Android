package com.Meditation.Sounds.frequencies.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.AttrRes
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import com.Meditation.Sounds.frequencies.R

/**
 * Created by mrshi on 17-May-17.
 */

class CustomSeekBar : FrameLayout {
    private lateinit var mPaintProgress: Paint
    private lateinit  var mPaintBackground: Paint
    private lateinit  var mThumb: RoundedView
    private var mThumbSize: Int = 0
    private var mProgressHeight: Int = 0
    private var mProgressBackgroundHeight: Int = 0
    private var mBottomMargin: Int = 0
    private var mTopMargin: Int = 0
    private lateinit  var mRect: RectF

    var progress: Int = 0
        private set
    var max = 9
        private set
    private var translateX: Float = 0.toFloat()

    private var mListener: OnProgressChangedListener? = null


    constructor(context: Context) : super(context, null) {
        init( null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    internal fun init(attrs: AttributeSet?, @AttrRes defStyleAttr: Int) {

        mPaintProgress = Paint()
        mPaintProgress.isAntiAlias = true
        mPaintProgress.style = Paint.Style.FILL

        mPaintBackground = Paint()
        mPaintBackground.isAntiAlias = true
        mPaintBackground.style = Paint.Style.FILL

        if(attrs!=null){
            val a = context.obtainStyledAttributes(attrs,
                    R.styleable.CustomSeekBar, defStyleAttr, 0)
            progress = a.getInteger(R.styleable.CustomSeekBar_csb_progress, 0)
            max = a.getInteger(R.styleable.CustomSeekBar_csb_max, 100)
            mThumbSize = a.getDimensionPixelSize(R.styleable.CustomSeekBar_csb_thumb_size, resources.getDimensionPixelSize(R.dimen.thumb_size))
            mProgressHeight = a.getDimensionPixelSize(R.styleable.CustomSeekBar_csb_progress_height, resources.getDimensionPixelSize(R.dimen.progress_height))
            mProgressBackgroundHeight = a.getDimensionPixelSize(R.styleable.CustomSeekBar_csb_progress_background_height, resources.getDimensionPixelSize(R.dimen.progress_height))

            mPaintProgress.color = a.getColor(R.styleable.CustomSeekBar_csb_progress_color, Color.WHITE)

            mPaintBackground.color = a.getColor(R.styleable.CustomSeekBar_csb_progress_color, Color.WHITE)
            a.recycle()
        }else{
            progress = 0
            max = 100
            mThumbSize = resources.getDimensionPixelSize(R.dimen.thumb_size)
            mProgressHeight = resources.getDimensionPixelSize(R.dimen.progress_height)
            mProgressBackgroundHeight = resources.getDimensionPixelSize(R.dimen.progress_height)

            mPaintProgress.color = Color.WHITE

            mPaintBackground.color = Color.WHITE
        }




        mBottomMargin = resources.getDimensionPixelSize(R.dimen.progress_margin)
        mTopMargin = mBottomMargin

        minimumHeight = mThumbSize
        minimumWidth = mThumbSize

        //addThumb
        mThumb = RoundedView(context)
        mThumb.roundedBackgroundColor = Color.WHITE
        mThumb.isClickable = false
        mThumb.isDuplicateParentStateEnabled = true

        val params = FrameLayout.LayoutParams(mThumbSize, mThumbSize)
        params.gravity = Gravity.CENTER_VERTICAL
        addView(mThumb, params)




        mRect = RectF()

        setWillNotDraw(false)

    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isEnabled) {
            parent.requestDisallowInterceptTouchEvent(true)
            val progressWidth = width - mThumbSize
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val centerY = height / 2
//                    val thumbRectangle = Rectangle()
//                    thumbRectangle.setBounds(0, centerY - mThumbSize / 2,
//                            width, centerY + mThumbSize / 2)
//                    if (thumbRectangle.contains(event.x.toInt(), event.y.toInt())) {
//                        parent.requestDisallowInterceptTouchEvent(true)
                        translateX = event.x - mThumbSize / 2
                        if (translateX < 0) {
                            translateX = 0f
                        } else if (translateX > progressWidth) {
                            translateX = progressWidth.toFloat()
                        }
                        mThumb.translationX = translateX
                        invalidate()
                        if (mListener != null) {
                            mListener!!.onProgressChanged(Math.round(max * translateX / progressWidth), max, true)
                        }
//                    } else {
//                        return false
//                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    translateX = event.x - mThumbSize / 2
                    if (translateX < 0) {
                        translateX = 0f
                    } else if (translateX > progressWidth) {
                        translateX = progressWidth.toFloat()
                    }
                    mThumb.translationX = translateX
                    invalidate()
                    if (mListener != null) {
                        mListener!!.onProgressChanged(Math.round(max * translateX / progressWidth), max, true)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    parent.requestDisallowInterceptTouchEvent(false)
                    translateX = event.x - mThumbSize / 2
                    if (translateX < 0) {
                        translateX = 0f
                    } else if (translateX > progressWidth) {
                        translateX = progressWidth.toFloat()
                    }
                    setProgress(Math.round(max * translateX / progressWidth), max)
                    mListener!!.onProgressDone()
                }
            }//                }
            return true
        }
        return false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0) {
            post { setProgress(progress, max) }
        }
    }


    fun setColor(backgroundColor: Int, progressColor: Int) {
        mPaintProgress.color = progressColor
        mPaintBackground.color = backgroundColor
        invalidate()
    }

    fun setOnProgressChangedListener(listener: OnProgressChangedListener) {
        mListener = listener
    }

    fun setProgress(progress: Int, max: Int) {
        this.progress = progress
        this.max = max
        val progressWidth = width - mThumbSize
        translateX = progressWidth.toFloat() * progress.toFloat() * 1.0f / max
        mThumb.translationX = translateX
        invalidate()
        if (mListener != null) {
            mListener!!.onProgressChanged(progress, max, false)
        }

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerY = height/2

        //Draw progress
        mRect.top = (centerY - mProgressHeight / 2).toFloat()
        mRect.bottom = (centerY + mProgressHeight / 2).toFloat()
        mRect.left = (mThumbSize / 2).toFloat()
        mRect.right = mThumbSize / 2 + translateX

        canvas.drawRoundRect(mRect, mProgressHeight.toFloat(), mProgressHeight.toFloat(), mPaintProgress)


        //Draw progress
        mRect.top = (centerY - mProgressBackgroundHeight / 2).toFloat()
        mRect.bottom = (centerY + mProgressBackgroundHeight / 2).toFloat()
        mRect.left = mThumbSize / 2 + translateX
        mRect.right = (width - mThumbSize / 2).toFloat()

        canvas.drawRoundRect(mRect, mProgressHeight.toFloat(), mProgressHeight.toFloat(), mPaintBackground)


    }

    interface OnProgressChangedListener {
        fun onProgressChanged(progress: Int, max: Int, isFromUser: Boolean)
        fun onProgressDone()
    }

}
