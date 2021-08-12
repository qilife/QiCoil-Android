package com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.Meditation.Sounds.frequencies.lemeor.data.model.Category

class CategoriesPagerAdapter(
        private val fragment: CategoriesPagerFragment,
        fm: FragmentManager,
        private val categoriesList: ArrayList<Category>
        ) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return AlbumsRecyclerFragment.newInstance(categoriesList[position].id, fragment)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return categoriesList[position].name
    }

    override fun getCount(): Int { return categoriesList.size }
}