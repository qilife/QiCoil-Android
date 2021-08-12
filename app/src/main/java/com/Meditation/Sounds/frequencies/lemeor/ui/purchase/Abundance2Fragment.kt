package com.Meditation.Sounds.frequencies.lemeor.ui.purchase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.adapters.Abundance2Adapter
import com.Meditation.Sounds.frequencies.adapters.TitleAdapter
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs.AlbumsViewModel
import kotlinx.android.synthetic.main.fragment_abundance2.*
import java.util.*

class Abundance2Fragment : Fragment() {

    private lateinit var mViewModel: AlbumsViewModel
    private var titleAdapter: TitleAdapter? = null
    private var abundance2Adapter: Abundance2Adapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_abundance2, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mViewModel = ViewModelProvider(this,
                ViewModelFactory(
                        ApiHelper(RetrofitBuilder(requireContext()).apiService),
                        DataBase.getInstance(requireContext()))
        ).get(AlbumsViewModel::class.java)

        val titleList = ArrayList<String>()
        titleList.add(getString(R.string.tv_title_advanced2_1))
        titleList.add(getString(R.string.tv_title_advanced2_2))
        titleList.add(getString(R.string.tv_title_advanced2_3))
        titleList.add(getString(R.string.tv_title_advanced2_4))
        titleList.add(getString(R.string.tv_title_advanced2_5))
        titleList.add(getString(R.string.tv_title_advanced2_6))
        titleList.add(getString(R.string.tv_title_advanced2_7))
        titleList.add(getString(R.string.tv_title_advanced2_8))
        titleList.add(getString(R.string.tv_title_advanced2_9))
        titleList.add(getString(R.string.tv_title_advanced2_10))
        titleList.add(getString(R.string.tv_title_advanced2_11))
        titleList.add(getString(R.string.tv_title_advanced2_12))
        titleList.add(getString(R.string.tv_title_advanced2_13))

        titleAdapter = TitleAdapter(titleList)
        abundanceII_recycler_view.adapter = titleAdapter

        val images = ArrayList<Album>()
        abundance2Adapter = Abundance2Adapter(requireContext(), images)
        abundanceII_images_recycler_view.adapter = abundance2Adapter
        abundanceII_images_recycler_view.setHasFixedSize(true)
        abundanceII_images_recycler_view.setItemViewCacheSize(20)

        // anbundance II - category id = 3
        mViewModel.albums(3)?.observe(viewLifecycleOwner, {
            abundance2Adapter!!.setData(it)
        })
    }
}