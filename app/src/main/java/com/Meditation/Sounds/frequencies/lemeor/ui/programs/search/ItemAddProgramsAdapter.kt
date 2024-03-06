package com.Meditation.Sounds.frequencies.lemeor.ui.programs.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.loadImage
import kotlinx.android.synthetic.main.item_add_programs.view.cbItem
import kotlinx.android.synthetic.main.item_add_programs.view.imgLock
import kotlinx.android.synthetic.main.item_add_programs.view.imgView
import kotlinx.android.synthetic.main.item_add_programs.view.imgViewAlbum
import kotlinx.android.synthetic.main.item_add_programs.view.tvDes
import kotlinx.android.synthetic.main.item_add_programs.view.tvName

class ItemAddProgramsAdapter(
    private val onSelected: (Search) -> Unit,
    private val mListSelected: ArrayList<Search> = arrayListOf()
) : ListAdapter<Search, ItemAddProgramsAdapter.ViewHolder>(SearchDiffCallback()) {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(item: Search) {
            with(view) {
                cbItem.visibility = View.VISIBLE
                if (item.obj is Rife) {
                    val r = item.obj as Rife
                    imgViewAlbum.visibility = View.GONE
                    imgLock.visibility = View.GONE
                    imgView.visibility = View.VISIBLE
                    imgView.setBackgroundDrawable(context.getDrawable(R.drawable.frequency))
                    tvName.text = r.title.trim()
                    tvDes.text = context.getString(R.string.tv_from_rife)
                    itemView.setOnClickListener {
                        if (item in mListSelected) {
                            mListSelected.remove(item)
                        } else {
                            mListSelected.add(item)
                        }
                        cbItem.isChecked = item in mListSelected
                        onSelected.invoke(item)
                    }
                    cbItem.setOnClickListener {
                        itemView.performClick()
                    }
                } else if (item.obj is Track) {
                    val t = item.obj as Track
                    tvName.text = t.name.trim()
                    imgViewAlbum.visibility = View.VISIBLE
                    imgLock.isVisible = !t.isUnlocked
                    imgView.visibility = View.GONE
                    t.album?.let { album ->
                        loadImage(context, imgViewAlbum, album)
                        tvDes.text = album.name
                    }
                    if (t.isUnlocked) {
                        itemView.setOnClickListener {
                            if (item in mListSelected) {
                                mListSelected.remove(item)
                            } else {
                                mListSelected.add(item)
                            }
                            cbItem.isChecked = item in mListSelected
                            onSelected.invoke(item)
                        }
                        cbItem.setOnClickListener {
                            itemView.performClick()
                        }
                    } else {
                        cbItem.visibility = View.INVISIBLE
                    }
                }
                cbItem.isChecked = item in mListSelected
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_add_programs, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    private class SearchDiffCallback : DiffUtil.ItemCallback<Search>() {
        override fun areItemsTheSame(oldItem: Search, newItem: Search): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Search, newItem: Search): Boolean {
            return oldItem == newItem
        }
    }
}