package com.Meditation.Sounds.frequencies.lemeor.ui.albums.search

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Category
import kotlinx.android.synthetic.main.item_search.view.*

class AlbumsSearchAdapter(
    private var mData: List<Album> = arrayListOf(),
) : RecyclerView.Adapter<AlbumsSearchAdapter.ViewHolder>() {

    private var groupAlbum: Map<String, Int>? = null
    private var categories = arrayListOf<Category>()

    interface Listener {
        fun onAlbumSearchClick(album: Album, i: Int)
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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = mData[position]

        if (groupAlbum?.isNotEmpty() == true && (groupAlbum!![album.name] ?: 0) > 1 && categories.isNotEmpty()) {
            holder.itemView.tvSearch.text = album.name + "/" + categories.first { it.id == album.category_id }.name
        } else {
            holder.itemView.tvSearch.text = album.name
        }
        holder.itemView.setOnClickListener { mListener?.onAlbumSearchClick(album, position) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    @SuppressLint("NotifyDataSetChanged")
    fun setData(albumList: List<Album>?) {
        mData = albumList as ArrayList<Album>
        groupAlbum = mData.groupingBy { it.name }.eachCount()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setCategories(data: List<Category>) {
        categories.clear()
        categories.addAll(data)
        notifyDataSetChanged()
    }

    fun getCategories():List<Category> {
        return categories
    }
}