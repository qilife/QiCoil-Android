package com.Meditation.Sounds.frequencies.lemeor.ui.programs.search

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.Meditation.Sounds.frequencies.lemeor.ui.main.HomeViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail.ProgramDetailFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_add_programs.btnAdd
import kotlinx.android.synthetic.main.fragment_add_programs.btnBack
import kotlinx.android.synthetic.main.fragment_add_programs.tabs
import kotlinx.android.synthetic.main.fragment_add_programs.viewPager

class AddProgramsFragment : BaseFragment() {
    override fun initLayout() = R.layout.fragment_add_programs
    private val programId: Int by lazy {
        arguments?.getInt(ARG_PROGRAM_ID)
            ?: throw IllegalArgumentException("Must call through newInstance()")
    }
    private val isRife: Int by lazy {
        arguments?.getInt(ARG_IS_RIFE)
            ?: throw IllegalArgumentException("Must call through newInstance()")
    }
    private lateinit var mViewModel: HomeViewModel
    private lateinit var mNewProgramViewModel: NewProgramViewModel
    private val listSelected = arrayListOf<Search>()
    private val adapter by lazy {
        AddProgramsAdapter {
            listSelected.clear()
            listSelected.addAll(it)
        }
    }

    override fun initComponents() {
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[HomeViewModel::class.java]
        mNewProgramViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[NewProgramViewModel::class.java]
        syncData()
    }

    private fun syncData() {
        mViewModel.getData(
            PreferenceHelper.preference(
                requireContext()
            )
        ) { listA, listR ->
            val list = arrayListOf<Pair<String, List<Search>>>()
            list.add(Pair("Track", listA))
            list.add(Pair("Rife", listR))
            setupViewPager(list)
        }
    }

    override fun addListener() {
        btnBack.setOnClickListener {
            onBackPressed()
        }
        btnAdd.setOnClickListener {
            if (listSelected.isNotEmpty()) {
                mNewProgramViewModel.addTrackToProgram(programId, listSelected)
            }
        }
    }

    private fun setupViewPager(list: List<Pair<String, List<Search>>>) {
        viewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        viewPager.adapter = adapter
        adapter.setListContents(list)
        viewPager.isUserInputEnabled = false
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = list[position].first
        }.attach()
        tabs.selectTab(tabs.getTabAt(isRife))
    }

    private fun onBackPressed() {
        val fragment = ProgramDetailFragment.newInstance(programId)
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.trans_left_to_right_in, R.anim.trans_left_to_right_out)
            .replace(R.id.nav_host_fragment, fragment, fragment.javaClass.simpleName).commit()
    }

    companion object {
        const val ARG_PROGRAM_ID = "arg_program"
        const val ARG_IS_RIFE = "arg_is_rife"

        @JvmStatic
        fun newInstance(id: Int, isRife: Int = 0) = AddProgramsFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PROGRAM_ID, id)
                putInt(ARG_IS_RIFE, isRife)
            }
        }
    }
}