package com.Meditation.Sounds.frequencies.lemeor.ui.rife.tabs

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import kotlinx.android.synthetic.main.item_rife_program.view.*

class ProgramAdapter(
    val onClick: (Rife) -> Unit
) : RecyclerView.Adapter<ProgramAdapter.ViewHolder>() {
    private var listRife = arrayListOf<Rife>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_rife_program, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listRife.size
    }

    @SuppressLint("SetTextI18n", "StringFormatInvalid")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rife = listRife[position]

        holder.itemView.tvTitle.text = rife.title
        holder.itemView.tvLineTime.text = holder.itemView.context.getString(
            R.string.tv_count_frequencies, rife.getFrequency().size.toString()
        )
        holder.itemView.setOnClickListener { onClick.invoke(rife) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    @SuppressLint("NotifyDataSetChanged")
    fun setData(rifeList: List<Rife>?) {
        listRife = rifeList as ArrayList<Rife>
//        groupAlbum = mData.groupingBy { it.name }.eachCount()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setListRife(data: List<Rife>) {
        listRife.clear()
        listRife.addAll(data)
        notifyDataSetChanged()
    }

    fun getListRife(): List<Rife> {
        return listRife
    }

}