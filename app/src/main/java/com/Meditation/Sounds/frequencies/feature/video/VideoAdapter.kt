package com.Meditation.Sounds.frequencies.feature.video

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.models.Video
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_video.view.*

class VideoAdapter(private val mContext: Context?, var mData: ArrayList<Video>) : RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    private var mListener: IOnClickItemListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false))
    }

    fun setOnClickListener(listener: IOnClickItemListener) {
        mListener = listener
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val video = mData[position]
        holder.itemView.mTvTitleVideo.text = video.title
        if (mContext != null) {
            Picasso.get().load(video.thumbnails).placeholder(R.drawable.ic_no_thumbnail).into(holder.itemView.mImvVideo)
        }

        holder.itemView.setOnClickListener {
            mListener?.onClickItem(video, position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface IOnClickItemListener {
        fun onClickItem(video: Video, position: Int)
    }

    fun setListVideo(listVideo: ArrayList<Video>) {
        mData = listVideo
        notifyDataSetChanged()
    }
}
