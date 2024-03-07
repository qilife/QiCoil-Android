package com.Meditation.Sounds.frequencies.lemeor.ui.programs.search

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search
import com.Meditation.Sounds.frequencies.utils.loadImageWithGif
import kotlinx.android.synthetic.main.view_categories_add_programs.view.ivImage
import kotlinx.android.synthetic.main.view_categories_add_programs.view.loadingFrame
import kotlinx.android.synthetic.main.view_categories_add_programs.view.rcvSearch

class AddProgramsAdapter(
    private val fm: FragmentActivity,
    private val onChanged: (List<Search>) -> Unit
) :
    RecyclerView.Adapter<AddProgramsAdapter.ViewHolder>() {

    private var listContents: List<Triple<String, List<Search>, Boolean>> = listOf()
    private val listSelected: ArrayList<Search> = arrayListOf()
    override fun getItemCount(): Int = listContents.size


    @SuppressLint("NotifyDataSetChanged")
    fun setListContents(list: List<Triple<String, List<Search>, Boolean>>) {
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
        fun bindData(data: Triple<String, List<Search>, Boolean>) {
            with(view) {
                loadImageWithGif(ivImage, R.raw.loading_grey)
                loadingFrame.isVisible = data.third
                rcvSearch.isVisible = !data.third
                setupRecyclerView(rcvSearch, context, data.second)
            }
        }
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView, context: Context, list: List<Search>
    ) {
        recyclerView.apply {
            adapter = ItemAddProgramsAdapter(mListSelected = listSelected, onSelected = { _ ->
                onChanged.invoke(listSelected)
            }, fm = fm).apply {
                this.submitList(list)
            }
            itemAnimator = null
        }
    }
}