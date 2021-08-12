package com.Meditation.Sounds.frequencies.lemeor.ui.albums.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Tag
import kotlinx.android.synthetic.main.item_search.view.*

class TagsSearchAdapter(
        private val mContext: Context,
        private var mData: List<Tag>,
) : RecyclerView.Adapter<TagsSearchAdapter.ViewHolder>() {

    interface Listener {
        fun onCategoriesSearchClick(tag: Tag, i: Int)
    }

    private var mListener: Listener? = null

    fun setOnClickListener(listener: Listener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false))
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = mData[position]
        holder.itemView.tvSearch.text = tag.name
        holder.itemView.setOnClickListener { mListener?.onCategoriesSearchClick(tag, position) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun setData(tagList: List<Tag>?) {
        mData = tagList as ArrayList<Tag>
        notifyDataSetChanged()
    }
}