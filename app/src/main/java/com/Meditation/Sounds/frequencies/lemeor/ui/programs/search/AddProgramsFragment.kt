package com.Meditation.Sounds.frequencies.lemeor.ui.programs.search

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.hideKeyboard
import com.Meditation.Sounds.frequencies.lemeor.ui.main.HomeViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.NewProgramViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail.ProgramDetailFragment
import com.Meditation.Sounds.frequencies.utils.FlowSearch
import com.Meditation.Sounds.frequencies.utils.safeOnClickListener
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_add_programs.btnAdd
import kotlinx.android.synthetic.main.fragment_add_programs.btnBack
import kotlinx.android.synthetic.main.fragment_add_programs.btnClear
import kotlinx.android.synthetic.main.fragment_add_programs.searchHint
import kotlinx.android.synthetic.main.fragment_add_programs.tabs
import kotlinx.android.synthetic.main.fragment_add_programs.viewPager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class AddProgramsFragment : BaseFragment() {
    override fun initLayout() = R.layout.fragment_add_programs
    private val programId: Int by lazy {
        arguments?.getInt(ARG_PROGRAM_ID)
            ?: throw IllegalArgumentException("Must call through newInstance()")
    }
    private val isRife: Int by lazy {
        arguments?.getInt(ARG_IS_RIFE)
            ?: 0
    }
    private var page = 0
    private lateinit var mViewModel: HomeViewModel
    private lateinit var mNewProgramViewModel: NewProgramViewModel
    private val listSelected = arrayListOf<Search>()
    private val adapter by lazy {
        AddProgramsAdapter(requireActivity()) {
            listSelected.clear()
            listSelected.addAll(it)
        }
    }

    override fun initComponents() {
        page = isRife
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[HomeViewModel::class.java]
        mNewProgramViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[NewProgramViewModel::class.java]
        setupViewPager()
        syncData()
    }

    private fun syncData() {
        mViewModel.apply {
            getLiveData(viewLifecycleOwner, requireContext())
            pairData.observe(viewLifecycleOwner) { list ->
                resetData(list)
            }
        }
    }

    @OptIn(FlowPreview::class)
    override fun addListener() {
        btnBack.setOnClickListener {
            onBackPressed()
        }
        btnAdd.safeOnClickListener {
            if (listSelected.isNotEmpty()) {
                onBackPressed()
                mNewProgramViewModel.addTrackToProgram(programId, listSelected)
            }
        }
        FlowSearch.fromSearchView(searchHint).debounce(200).map { text -> text.trim() }
            .distinctUntilChanged().asLiveData().observe(this) {
                btnClear.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                mViewModel.setSearchKeyword(
                    key = it,
                    tabs.selectedTabPosition,
                    requireContext()
                ) { list ->
                    adapter.setListContents(list)
                }
            }

        btnClear.safeOnClickListener {
            btnClear.visibility = View.GONE
            searchHint.setText("")
            searchHint.clearFocus()
            hideKeyboard(requireContext(), searchHint)
        }
    }

    private fun setupViewPager() {
        viewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                page = position
                searchHint.setText("")
                searchHint.clearFocus()
                hideKeyboard(requireContext(), searchHint)
                super.onPageSelected(position)
            }
        })
    }

    private fun resetData(list: List<Triple<String, List<Search>, Boolean>>) {
        adapter.setListContents(list)
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = list[position].first
        }.attach()
        tabs.selectTab(tabs.getTabAt(page))
        viewPager.currentItem = page
    }

    private fun onBackPressed() {
        try {
            hideKeyboard(requireContext(), searchHint)
        } catch (_: Exception) {
        }
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