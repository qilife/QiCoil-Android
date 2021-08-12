package com.Meditation.Sounds.frequencies.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Meditation.Sounds.frequencies.R
import kotlinx.android.synthetic.main.item_title_higher.view.*

class TitleAdapter(var data: ArrayList<String>) : RecyclerView.Adapter<TitleAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_title_higher, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.itemView.tvTitleHigher.text = item
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
