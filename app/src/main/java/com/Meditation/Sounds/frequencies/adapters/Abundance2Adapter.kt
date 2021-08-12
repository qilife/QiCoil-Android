package com.Meditation.Sounds.frequencies.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.loadImage
import kotlinx.android.synthetic.main.item_album_higher.view.*

class Abundance2Adapter(var context: Context, var data: ArrayList<Album>) : RecyclerView.Adapter<Abundance2Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_album_higher, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = data[position]

        loadImage(context, holder.itemView.image, album)
    }

    fun setData(categoryList: List<Album>?) {
        data = categoryList as ArrayList<Album>
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
