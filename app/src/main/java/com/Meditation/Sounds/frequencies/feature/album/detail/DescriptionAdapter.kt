package com.Meditation.Sounds.frequencies.feature.album.detail

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Meditation.Sounds.frequencies.R
import kotlinx.android.synthetic.main.item_description.view.*

class DescriptionAdapter(var context: Context, var data: List<String>) : RecyclerView.Adapter<DescriptionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_description, parent, false))
    }

    override fun getItemCount(): Int { return data.size }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val description = data[position]
        holder.itemView.description.text = description
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
