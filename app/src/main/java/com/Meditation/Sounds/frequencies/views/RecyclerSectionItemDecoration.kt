package com.Meditation.Sounds.frequencies.views

import android.graphics.Canvas
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import kotlin.math.max

class RecyclerSectionItemDecoration(
    headerHeight: Int,
    private var sticky: Boolean,
    private var top: Int = 0,
    private var left: Int = 0,
    private var right: Int = 0,
    private var bottom: Int = 0,
) : RecyclerView.ItemDecoration() {
    private var sectionCallBack: SectionCallBack? = null
    private var headerOffSet: Int = headerHeight
    private var recyclerView: RecyclerView? = null
    private lateinit var headerView: View
    private lateinit var header: TextView

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {

        val pos: Int = parent.getChildAdapterPosition(view)
        if (sectionCallBack != null) {
            if (sectionCallBack!!.isSection(pos)) {
                outRect.top = headerOffSet + top
                outRect.left = left
                outRect.right = right
                outRect.bottom = bottom
            } else {
                outRect.left = left
                outRect.right = right
                outRect.bottom = bottom
            }
        }
    }

    fun setParent(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    fun addSectionCallBack(sectionCallBack: SectionCallBack) {
        this.sectionCallBack = sectionCallBack
    }

    fun notifyDataChanged() {
        if (recyclerView != null) {
            headerView = inflateHeaderView(parent = recyclerView!!)
            header = headerView.findViewById(R.id.mTextView)
            fixLayoutSize(headerView, recyclerView!!)
            recyclerView!!.invalidateItemDecorations()
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        headerView = inflateHeaderView(parent)
        header = headerView.findViewById(R.id.mTextView)
        fixLayoutSize(headerView, parent)

        var previousHeader = ""
        if (sectionCallBack != null) {
            for (i in 0..parent.childCount) {
                if (parent.getChildAt(i) != null) {
                    val child: View = parent.getChildAt(i)
                    val position: Int = parent.getChildAdapterPosition(child)

                    val title: String = sectionCallBack!!.getSectionHeader(position)
                    header.text = title
                    if (previousHeader != title || sectionCallBack!!.isSection(position)) {
                        drawHeader(c, child, headerView)
                        previousHeader = title
                    }
                }
            }
        }
    }

    private fun drawHeader(c: Canvas, child: View, headerView: View) {
        c.save()
        if (sticky) {
            c.translate(0F, max(0, child.top - headerView.height - top).toFloat())
        } else {
            c.translate(0F, (child.top - headerView.height).toFloat())
        }
        headerView.draw(c)
        c.restore()
    }

    private fun inflateHeaderView(parent: RecyclerView): View {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.header_sticky_section, parent, false)
    }

    private fun fixLayoutSize(view: View, parent: ViewGroup) {
        val widthSpec: Int =
            View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec: Int =
            View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)
        val childWidth: Int = ViewGroup.getChildMeasureSpec(
            widthSpec,
            parent.paddingLeft + parent.paddingRight,
            view.layoutParams.width
        )
        val childHeight: Int =
            ViewGroup.getChildMeasureSpec(
                heightSpec,
                parent.paddingTop + parent.paddingBottom,
                view.layoutParams.height
            )

        view.measure(childWidth, childHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    interface SectionCallBack {
        fun isSection(position: Int): Boolean
        fun getSectionHeader(position: Int): String
    }
}