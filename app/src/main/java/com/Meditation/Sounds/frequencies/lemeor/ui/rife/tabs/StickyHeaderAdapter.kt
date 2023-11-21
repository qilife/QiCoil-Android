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
    private var categories = arrayListOf<String>()
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
        holder.itemView.tvTitle.setTextColor(Color.parseColor("#FFFFFF"))
        holder.itemView.tvTitle.textSize = 16F
        holder.itemView.setOnClickListener {
            val previous = selectedItem
            selectedItem = position
            notifyItemChanged(selectedItem)
            notifyItemChanged(previous)

            listener.invoke(tabHeader)
        }
//        if (selectedItem == position) {
//            holder.itemView.tvTitle.setTextColor(Color.parseColor("#059F83"))
//        } else {
//            holder.itemView.tvTitle.setTextColor(Color.parseColor("#FFFFFF"))
//        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    @SuppressLint("NotifyDataSetChanged")
    fun setData(albumList: List<String>?) {
        categories = albumList as ArrayList<String>
        notifyDataSetChanged()
    }

}