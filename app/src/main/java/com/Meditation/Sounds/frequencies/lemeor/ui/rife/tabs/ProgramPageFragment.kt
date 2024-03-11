package com.Meditation.Sounds.frequencies.lemeor.ui.rife.tabs

import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.ui.main.HomeViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.NewRifeFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.NewRifeViewModel
import com.Meditation.Sounds.frequencies.utils.loadImageWithGif
import com.Meditation.Sounds.frequencies.views.RecyclerSectionItemDecoration
import kotlinx.android.synthetic.main.fragment_program_page.ivImage
import kotlinx.android.synthetic.main.fragment_program_page.loadingFrame
import kotlinx.android.synthetic.main.fragment_program_page.rcvProgram
import kotlinx.android.synthetic.main.fragment_program_page.rcvTabList
import kotlinx.android.synthetic.main.fragment_program_page.srlRife

class ProgramPageFragment : BaseFragment() {
    private lateinit var mViewModel: NewRifeViewModel
    private lateinit var mHomeViewModel: HomeViewModel
    private val sectionItemDecoration by lazy {
        RecyclerSectionItemDecoration(
            resources.getDimensionPixelSize(R.dimen.height_calander_icon),
            true,
            top = resources.getDimensionPixelSize(R.dimen.margin_item_10),
            left = resources.getDimensionPixelSize(R.dimen.margin_item_10),
            right = resources.getDimensionPixelSize(R.dimen.margin_item_10),
            bottom = resources.getDimensionPixelSize(R.dimen.margin_item_10),
        )
    }
    var mainFm: NewRifeFragment? = null

    private val mProgramAdapter by lazy {
        ProgramAdapter {
            if (mainFm != null) {
                mainFm!!.openAlbum(it)
            }
        }
    }
    private var listRife = listOf<Rife>()
    private val mStickyHeaderAdapter by lazy {
        StickyHeaderAdapter { tab ->
            val positionToScroll = listRife.indexOfFirst {
                if (tab.first().isLetter()) tab.uppercase().first() == it.title.uppercase()
                    .first() else !it.title.first().isLetter()
            }
            val layoutManager = rcvProgram.layoutManager
            layoutManager?.scrollToPosition(positionToScroll)

            rcvProgram.post {
                val child = layoutManager?.findViewByPosition(positionToScroll)
                child?.let {
                    val offsetY =
                        rcvProgram.context.resources.getDimensionPixelOffset(R.dimen.indent_40)
                    rcvProgram.scrollBy(0, it.top - offsetY)
                }
            }
        }
    }

    override fun initLayout() = R.layout.fragment_program_page

    override fun initComponents() {
        initView()
    }

    override fun addListener() {
        mViewModel.apply {
            getRifeList().observe(viewLifecycleOwner) { listRift ->
                val list = listRift.sortedWith(compareBy<Rife> {
                    when {
                        it.title.lowercase().firstOrNull()?.isLetter() == true -> 0
                        else -> 1
                    }
                }.thenBy { it.title.lowercase() })
                listRife = list
                mProgramAdapter.setListRife(list)
                sectionItemDecoration.addSectionCallBack(RecyclerSectionItemDecorator(list))
                sectionItemDecoration.notifyDataChanged()
                val uniqueFirstChars = list.map {
                    if (it.title.uppercase().first().isLetter()) it.title.uppercase().first()
                        .toString()
                    else "#"
                }.distinct().toList()
                mStickyHeaderAdapter.setData(uniqueFirstChars)
                loadingFrame.visibility = View.GONE
            }
        }

        srlRife.setOnRefreshListener {
            refreshData()
        }
    }


    private fun initView() {
        loadingFrame.visibility = View.VISIBLE
        loadImageWithGif(ivImage, R.raw.loading_grey)
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[NewRifeViewModel::class.java]
        mHomeViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[HomeViewModel::class.java]
        sectionItemDecoration.setParent(rcvProgram)
        rcvProgram.adapter = mProgramAdapter
        rcvProgram.itemAnimator = null
        rcvProgram.addItemDecoration(sectionItemDecoration)

        rcvTabList.adapter = mStickyHeaderAdapter
        rcvTabList.itemAnimator = null
    }

    private fun refreshData() {
        srlRife.visibility = View.GONE
        try {
            mHomeViewModel.getRife().observe(viewLifecycleOwner) {}
        } catch (_: Exception) {
        }
        srlRife.visibility = View.VISIBLE
        srlRife.isRefreshing = false
    }

    companion object {
        @JvmStatic
        fun newInstance(fm: NewRifeFragment): ProgramPageFragment {
            return ProgramPageFragment().apply {
                mainFm = fm
            }
        }
    }

    class RecyclerSectionItemDecorator(private var listRife: List<Rife>) :
        RecyclerSectionItemDecoration.SectionCallBack {
        override fun isSection(position: Int): Boolean {
            return if (listRife.isNotEmpty()) {
                if (position == 0) {
                    true
                } else if (listRife[position].title.lowercase().firstOrNull()
                        ?.isLetter() == false
                ) {
                    listRife[position].title.lowercase().firstOrNull()
                        ?.isLetter() == false && listRife[position - 1].title.lowercase()
                        .firstOrNull()?.isLetter() == true
                } else {
                    listRife[position].title.lowercase()
                        .codePointAt(0) != listRife[position - 1].title.lowercase().codePointAt(0)
                }
            } else false
        }

        override fun getSectionHeader(position: Int): String {
            return if (listRife.isNotEmpty()) {
                val sub = listRife[position].title.uppercase()[0]
                if (sub.isLetter()) {
                    sub.toString()
                } else {
                    "#"
                }
            } else {
                ""
            }
        }
    }
}