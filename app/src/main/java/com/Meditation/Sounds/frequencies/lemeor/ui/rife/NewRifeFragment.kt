package com.Meditation.Sounds.frequencies.lemeor.ui.rife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.tierPosition
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.NewAlbumDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.main.NavigationActivity
import com.Meditation.Sounds.frequencies.utils.Constants
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_new_rife.*

class NewRifeFragment : Fragment() {
    private lateinit var mViewModel: NewRifeViewModel
    var rifePagerAdapter: RifePagerAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_rife, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        view.isFocusableInTouchMode = true
        view.requestFocus()
    }

    fun init() {
        mViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        ).get(NewRifeViewModel::class.java)

        rifePagerAdapter = RifePagerAdapter(
            activity as NavigationActivity,
            childFragmentManager, this
        )
        rife_view_pager.adapter = rifePagerAdapter
        rife_tabs.setupWithViewPager(rife_view_pager)
        rifePagerAdapter?.setData(
            arrayListOf(
                "Programs",
                "Frequencies",
            )
        )

        btnSearch.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .setCustomAnimations(
                    R.anim.trans_right_to_left_in,
                    R.anim.trans_right_to_left_out,
                    R.anim.trans_left_to_right_in,
                    R.anim.trans_left_to_right_out
                )
                .replace(
                    R.id.nav_host_fragment,
                    SearchRifeFragment.newInstance(),
                    SearchRifeFragment().javaClass.simpleName
                )
                .commit()
        }
//        mViewModel.tiers?.observe(viewLifecycleOwner) {
//            tiersPagerAdapter?.setData(it as ArrayList<Tier>)
//            if (tierPositionSelected != 0) {
//                tiers_view_pager.setCurrentItem(tierPositionSelected, false)
//            }
//        }

        rife_tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tierPosition = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    fun openAlbum(rife: Rife) {
        parentFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.trans_right_to_left_in, R.anim.trans_right_to_left_out,
                R.anim.trans_left_to_right_in, R.anim.trans_left_to_right_out
            )
            .replace(
                R.id.nav_host_fragment,
                NewAlbumDetailFragment.newInstance(1, 1, Constants.TYPE_RIFE, rife),
                NewAlbumDetailFragment().javaClass.simpleName
            )
            .commit()
    }

}

val item = Rife(
    1,
    "Abdominal Cramps 1",
    "demo",
    "url",
    0,
    "75,255,235,660,750",
    "0",
    "0",
    "",
    true,
    "url",
    false,
    "a"
)

val listTest = arrayListOf(
    item,
    item.copy(
        id = 2,
        title = "Abdominal Cramps 2"
    ),
    item.copy(
        id = 3,
        title = "Abdominal Cramps 3",
        frequencies = ""
    ),
    item.copy(
        id = 4,
        title = "Abdominal Cramps 4"
    ),
    item.copy(
        id = 5,
        title = "Abdominal Cramps 5"
    ),
    item.copy(
        id = 6,
        title = "Abdominal Cramps 6"
    ),
    item.copy(
        id = 7,
        title = "Abdominal Cramps 7"
    ),
)