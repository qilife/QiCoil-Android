package com.Meditation.Sounds.frequencies.feature.playlist

import android.view.View
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.utils.Utils

/**
 * Created by DC-MEN on 8/18/2018.
 */
class PlaylistGroupFragment : BaseFragment(){

    override fun initLayout(): Int {
        return R.layout.fragment_playlist_group
    }

    override fun initComponents() {
        setNewPage(PlaylistFragment(), R.id.frame1)
        if(!Utils.isTablet(mContext)){
            hiddenAlbumFragment()
        }
    }

    fun hiddenAlbumFragment(){
        mView!!.findViewById<View>(R.id.frame2).visibility = View.GONE
    }

    fun showAlbumFragment(){
        mView!!.findViewById<View>(R.id.frame2).visibility = View.VISIBLE
    }

    fun isShowAlbumFragment() : Boolean{
        return mView!!.findViewById<View>(R.id.frame2).visibility == View.VISIBLE
    }

    override fun addListener() {

    }

}