package com.Meditation.Sounds.frequencies.lemeor.ui.programs.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import kotlinx.android.synthetic.main.item_add_programs.view.cbItem
import kotlinx.android.synthetic.main.item_add_programs.view.tvDes
import kotlinx.android.synthetic.main.item_add_programs.view.tvId
import kotlinx.android.synthetic.main.item_add_programs.view.tvName

class ItemAddProgramsAdapter(
    private val list: MutableList<Search>,
    private val onSelected: (Search) -> Unit,
    private val mListSelected: ArrayList<Search> = arrayListOf()
) : ListAdapter<Search, ItemAddProgramsAdapter.ViewHolder>(SearchDiffCallback()) {
    private val listSelected: ArrayList<Search> = arrayListOf()

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n")
        fun bind(item: Search) {
            with(view) {
                cbItem.visibility = View.VISIBLE
                if (item.obj is Rife) {
                    val r = item.obj as Rife
                    tvId.text = item.id.toString()
                    tvName.text = r.title.trim()
                    tvDes.text = "From Rife"
                    itemView.setOnClickListener {
                        if (item in listSelected) {
                            listSelected.remove(item)
                            mListSelected.remove(item)
                        } else {
                            listSelected.add(item)
                            mListSelected.add(item)
                        }
                        cbItem.isChecked = item in listSelected
                        onSelected.invoke(item)
                    }
                    cbItem.setOnClickListener {
                        itemView.performClick()
                    }
                } else if (item.obj is Track) {
                    val t = item.obj as Track
                    tvId.text = item.id.toString()
                    tvName.text = t.name.trim()
                    tvDes.text = "From Track"
                    if (t.isUnlocked) {
                        itemView.setOnClickListener {
                            if (item in listSelected) {
                                listSelected.remove(item)
                                mListSelected.remove(item)
                            } else {
                                listSelected.add(item)
                                mListSelected.add(item)
                            }
                            cbItem.isChecked = item in listSelected
                            onSelected.invoke(item)
                        }
                        cbItem.setOnClickListener {
                            itemView.performClick()
                        }
                    }else{
                        cbItem.visibility = View.INVISIBLE
                    }
                }
                cbItem.isChecked = item in listSelected

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_add_programs, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    private class SearchDiffCallback : DiffUtil.ItemCallback<Search>() {
        override fun areItemsTheSame(oldItem: Search, newItem: Search): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Search, newItem: Search): Boolean {
            return oldItem == newItem
        }
    }
}