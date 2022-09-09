package com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album

class AlbumsPagerAdapter(
        fm: FragmentManager,
        private val albumsList: ArrayList<Album>
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return PurchaseAlbumFragment.newInstance(albumsList[position])
    }

    override fun getPageTitle(position: Int): CharSequence {
        return albumsList[position].name
    }

    override fun getCount(): Int { return albumsList.size }
}