package com.Meditation.Sounds.frequencies.lemeor.ui.albums.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.*
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.search.SearchAdapter.ViewHolder
import kotlinx.android.synthetic.main.item_category_search.view.*
import kotlinx.android.synthetic.main.item_category_search.view.tvSearch
import kotlinx.android.synthetic.main.item_search.view.*

class SearchAdapter(private val onClick: (Search, Int) -> Unit) :
    ListAdapter<Search, ViewHolder>(
        AsyncDifferConfig.Builder(object : DiffUtil.ItemCallback<Search>() {
            override fun areItemsTheSame(
                oldItem: Search, newItem: Search
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: Search, newItem: Search
            ): Boolean {
                return oldItem == newItem
            }

        }).build()
    ) {

    private var categories = arrayListOf<Category>()
    private var groupAlbum: Map<String, Int>? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(item: Search, position: Int) {
            if (position == 0) {
                itemView.tvTitle.setPadding(0, 20, 0, 0)
                getTitle(item.obj, itemView)
            } else if (getItem(position - 1).obj.javaClass != getItem(position).obj.javaClass) {
                itemView.tvTitle.setPadding(0, 10, 0, 0)
                getTitle(item.obj, itemView)
            } else {
                itemView.tvTitle.visibility = View.GONE
            }
            if (item.obj is Album) {
                item.apply {
                    val album = item.obj as Album
                    if (groupAlbum != null && groupAlbum?.isNotEmpty() == true && (groupAlbum!![album.name]
                            ?: 0) > 1 && categories.isNotEmpty()
                    ) {
                        itemView.tvSearch.text =
                            album.name + "/" + categories.first { it.id == album.category_id }.name
                    } else {
                        itemView.tvSearch.text = album.name
                    }
                }

            } else {
                if (item.obj is Program) {
                    itemView.tvSearch.text = (item.obj as Program).name
                } else if (item.obj is Track) {
                    itemView.tvSearch.text = (item.obj as Track).name
                }else if(item.obj is Rife){
                    itemView.tvSearch.text = (item.obj as Rife).title
                }
            }
            itemView.tvSearch.setOnClickListener {
                onClick.invoke(item, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category_search, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setCategories(data: List<Category>) {
        categories.clear()
        categories.addAll(data)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setGroupAlbum(data: Map<String, Int>) {
        groupAlbum = null
        groupAlbum = data
        notifyDataSetChanged()
    }

    fun getCategories():List<Category> {
        return categories
    }

    fun getTitle(obj: Any, view: View) {
        view.apply {
            when (obj) {
                is Album -> {
                    tvTitle.text = view.context.getString(R.string.tv_albums)
                    tvTitle.visibility = View.VISIBLE
                }
                is Track -> {
                    tvTitle.text = view.context.getString(R.string.tv_frequencies)
                    tvTitle.visibility = View.VISIBLE
                }
                is Program -> {
                    tvTitle.text = view.context.getString(R.string.tv_programs)
                    tvTitle.visibility = View.VISIBLE
                }
                is Rife -> {
                    tvTitle.text = view.context.getString(R.string.tv_rifes)
                    tvTitle.visibility = View.VISIBLE
                }
                else -> {
                    tvTitle.visibility = View.GONE
                }
            }
        }
    }
}