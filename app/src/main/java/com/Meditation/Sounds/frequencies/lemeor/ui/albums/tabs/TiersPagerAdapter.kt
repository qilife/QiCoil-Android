package com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.Meditation.Sounds.frequencies.lemeor.data.model.Tier
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity

class TiersPagerAdapter(
        private val activity: NavigationActivity,
        fm: FragmentManager,
        private val tiersList: ArrayList<Tier>
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return CategoriesPagerFragment.newInstance(tiersList[position].id, activity)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return tiersList[position].name
    }

    override fun getCount(): Int { return tiersList.size }
}