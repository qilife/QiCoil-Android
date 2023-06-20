package com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.Meditation.Sounds.frequencies.lemeor.data.model.Tier
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity

class TiersPagerAdapter(
        private val activity: NavigationActivity,
        fm: FragmentManager
) : FragmentPagerAdapter(fm) {

    private val tiersList: ArrayList<Tier> = arrayListOf()

    override fun getItem(position: Int): Fragment {
        return CategoriesPagerFragment.newInstance(tiersList[position].id, activity)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return tiersList[position].name
    }

    override fun getCount(): Int { return tiersList.size }

    fun setData(data: ArrayList<Tier>){
        tiersList.clear()
        tiersList.addAll(data)
        notifyDataSetChanged()
    }
}