package com.Meditation.Sounds.frequencies.views

import android.content.Context
import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import androidx.annotation.DimenRes
import android.view.View

class ItemOffsetDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {

    constructor(context: Context, @DimenRes itemOffsetId: Int) : this(context.resources.getDimensionPixelSize(itemOffsetId))

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(
                mItemOffset, mItemOffset, mItemOffset, mItemOffset)
    }
}