package com.Meditation.Sounds.frequencies.views

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView


class ItemOffsetDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {

    constructor(
        context: Context,
        @DimenRes itemOffsetId: Int
    ) : this(context.resources.getDimensionPixelSize(itemOffsetId))

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(
            mItemOffset, mItemOffset, mItemOffset, mItemOffset
        )
    }
}


class ItemOffsetRightDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {

    constructor(
        context: Context,
        @DimenRes itemOffsetId: Int
    ) : this(context.resources.getDimensionPixelSize(itemOffsetId))

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val lastItemPosition = parent.adapter?.itemCount?.minus(1)
        if (position == lastItemPosition) {
            outRect.setEmpty()
        } else {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.set(
                0, 0, mItemOffset, 0
            )
        }
    }
}


class ItemOffsetBottomDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {

    constructor(
        context: Context,
        @DimenRes itemOffsetId: Int
    ) : this(context.resources.getDimensionPixelSize(itemOffsetId))

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        super.getItemOffsets(outRect, view, parent, state)
        if (position == 0) {
            outRect.set(
                0, mItemOffset, 0, mItemOffset
            )
        } else {
            outRect.set(
                0, 0, 0, mItemOffset
            )
        }

    }
}

class ItemLastOffsetBottomDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {

    constructor(
        context: Context,
        @DimenRes itemOffsetId: Int
    ) : this(context.resources.getDimensionPixelSize(itemOffsetId))

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val lastItemPosition = parent.adapter?.itemCount?.minus(1)
        super.getItemOffsets(outRect, view, parent, state)
        if (position == lastItemPosition) {
            outRect.bottom = mItemOffset
        }
    }
}