package com.Meditation.Sounds.frequencies.lemeor.ui.programs.search

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search

class AddProgramsAdapter(private val onChanged: (List<Search>) -> Unit) :
    RecyclerView.Adapter<AddProgramsAdapter.ViewHolder>() {

    private var listContents: List<Pair<String, List<Search>>> = listOf()
    private val listSelected: ArrayList<Search> = arrayListOf()
    override fun getItemCount(): Int = listContents.size


    @SuppressLint("NotifyDataSetChanged")
    fun setListContents(list: List<Pair<String, List<Search>>>) {
        listContents = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.view_categories_add_programs, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(listContents[position])
    }


    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bindData(data: Pair<String, List<Search>>) {
            with(view) {
                val rcvSearch = findViewById<RecyclerView>(R.id.rcvSearch)
                setupRecyclerView(rcvSearch, context, data.second)
            }
        }
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView, context: Context, list: List<Search>
    ) {
        recyclerView.apply {
            adapter = ItemAddProgramsAdapter(list = list.toMutableList(),
                mListSelected = listSelected,
                onSelected = { _ ->
                    onChanged.invoke(listSelected)
                }
            )
            itemAnimator = null
        }
    }
}