package com.Meditation.Sounds.frequencies.lemeor.ui.rife

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.tabs.FrequencyPageFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.tabs.ProgramPageFragment

class RifePagerAdapter(
    private val activity: NavigationActivity,
    private val fm: FragmentManager,
    private val nf: NewRifeFragment
) : FragmentPagerAdapter(fm) {

    private val rifeList: ArrayList<String> = arrayListOf()

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> ProgramPageFragment.newInstance(nf)
            1 -> FrequencyPageFragment.newInstance()
            else -> ProgramPageFragment.newInstance(nf)
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return rifeList[position]
    }

    override fun getCount(): Int {
        return rifeList.size
    }

    fun setData(data: ArrayList<String>) {
        rifeList.clear()
        rifeList.addAll(data)
        notifyDataSetChanged()
    }
}