package com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Category
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.hashMapTiers
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.AlbumsRecyclerFragment.AlbumsRecyclerListener
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_categories_pager.categories_tabs
import kotlinx.android.synthetic.main.fragment_categories_pager.categories_view_pager

class CategoriesPagerFragment : Fragment(), AlbumsRecyclerListener {

    interface CategoriesPagerListener {
        fun onAlbumDetails(album: Album)
        fun onLongAlbumDetails(album: Album)
    }

    private var mListener: CategoriesPagerListener? = null
    private lateinit var mViewModel: AlbumsViewModel
    private var tierId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tierId = arguments?.getInt(ARG_TIER_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_categories_pager, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initUI()
    }

    private fun initUI() {
        mViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        ).get(AlbumsViewModel::class.java)

        tierId?.let {
            mViewModel.categoriesByTierId(it)?.observe(viewLifecycleOwner) { list ->
                val categoriesPagerAdapter =
                    CategoriesPagerAdapter(this, childFragmentManager, list as ArrayList<Category>)
                categories_view_pager.adapter = categoriesPagerAdapter

                categories_view_pager.setCurrentItem(hashMapTiers[tierId] ?: 0, true)
                categories_tabs.setupWithViewPager(categories_view_pager)
            }
        }

        categories_tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tierId?.let { hashMapTiers.put(it, tab.position) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    companion object {
        private const val ARG_TIER_ID = "arg_tiers_id"

        @JvmStatic
        fun newInstance(tierId: Int, listener: CategoriesPagerListener): CategoriesPagerFragment {
            return CategoriesPagerFragment().apply {
                mListener = listener
                arguments = Bundle().apply {
                    putInt(ARG_TIER_ID, tierId)
                }
            }
        }
    }

    override fun onStartAlbumDetail(album: Album) {
        mListener?.onAlbumDetails(album)
    }

    override fun onStartLongAlbumDetail(album: Album) {
        mListener?.onLongAlbumDetails(album)
    }
}