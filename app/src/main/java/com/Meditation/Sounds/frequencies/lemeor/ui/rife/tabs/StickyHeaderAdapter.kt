package com.Meditation.Sounds.frequencies.lemeor.ui.rife.tabs

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import kotlinx.android.synthetic.main.item_string_frequency.view.*

class StickyHeaderAdapter(val listener: (String) -> Unit) :
    RecyclerView.Adapter<StickyHeaderAdapter.ViewHolder>() {
    var selectedItem = 0
    private var categories = listOf<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_string_frequency, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tabHeader = categories[position]

        holder.itemView.tvTitle.text = tabHeader
        holder.itemView.tvTitle.setPadding(20,0,20,0)
        holder.itemView.tvTitle.setTextColor(Color.parseColor("#FFFFFF"))
        holder.itemView.tvTitle.textSize = 14F
        holder.itemView.setOnClickListener {
            val previous = selectedItem
            selectedItem = position
            notifyItemChanged(selectedItem)
            notifyItemChanged(previous)

            listener.invoke(tabHeader)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    @SuppressLint("NotifyDataSetChanged")
    fun setData(albumList: List<String>?) {
        categories = albumList ?: listOf()
        notifyDataSetChanged()
    }

}