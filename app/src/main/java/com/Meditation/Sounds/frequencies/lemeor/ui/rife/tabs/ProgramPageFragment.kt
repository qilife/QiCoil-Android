package com.Meditation.Sounds.frequencies.lemeor.ui.rife.tabs

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
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.NewRifeFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.NewRifeViewModel
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.views.ItemOffsetBottomDecoration
import com.Meditation.Sounds.frequencies.views.RecyclerSectionItemDecoration
import kotlinx.android.synthetic.main.fragment_program_page.*

class ProgramPageFragment : Fragment() {
    private lateinit var mViewModel: NewRifeViewModel
    private var rifeId: Int? = null
    var mainFm: NewRifeFragment? = null
    private val mProgramAdapter = ProgramAdapter {
        if (mainFm != null) {
            mainFm!!.openAlbum(it)
        }
    }
    private var listRife = listOf<Rife>()
    private val mStickyHeaderAdapter = StickyHeaderAdapter { tab ->
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rifeId = arguments?.getInt(ProgramPageFragment.ARG_RIFE_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_program_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }


    private fun initView() {
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[NewRifeViewModel::class.java]

        rcvProgram.adapter = mProgramAdapter
        rcvProgram.itemAnimator = null
        rcvTabList.adapter = mStickyHeaderAdapter
        rcvTabList.itemAnimator = null
        mViewModel.getRifeList().observe(viewLifecycleOwner) { listRift ->
            val list = listRift.sortedWith(compareBy<Rife> {
                when {
                    it.title.lowercase().firstOrNull()?.isLetter() == true -> 0
                    else -> 1
                }
            }.thenBy { it.title.lowercase() })
            listRife = list
            val sectionItemDecoration = RecyclerSectionItemDecoration(
                resources.getDimensionPixelSize(R.dimen.height_calander_icon),
                true, RecyclerSectionItemDecorator(list),
                top = resources.getDimensionPixelSize(R.dimen.margin_item_10),
                left = resources.getDimensionPixelSize(R.dimen.margin_item_10),
                right = resources.getDimensionPixelSize(R.dimen.margin_item_10),
                bottom = resources.getDimensionPixelSize(R.dimen.margin_item_10),
            )
            mProgramAdapter.setListRife(list)
            rcvProgram.addItemDecoration(sectionItemDecoration)
            val uniqueFirstChars = list
                .map {
                    if (it.title.uppercase().first().isLetter())
                        it.title.uppercase().first().toString()
                    else "#"
                }
                .distinct()
                .toList()
            mStickyHeaderAdapter.setData(uniqueFirstChars)
        }
    }


    companion object {
        private const val ARG_RIFE_ID = "arg_rife_id"

        @JvmStatic
        fun newInstance(rifeId: Int, fm: NewRifeFragment): ProgramPageFragment {
            return ProgramPageFragment().apply {
                mainFm = fm
                arguments = Bundle().apply {
                    putInt(ARG_RIFE_ID, rifeId)
                }
            }
        }
    }

    class RecyclerSectionItemDecorator(private var listRife: List<Rife>) :
        RecyclerSectionItemDecoration.SectionCallBack {

        override fun isSection(position: Int): Boolean {
            if (listRife.isNotEmpty()) {
                return if (position == 0) {
                    true
                } else if (listRife[position].title.lowercase().firstOrNull()
                        ?.isLetter() == false
                ) {
                    listRife[position].title.lowercase().firstOrNull()
                        ?.isLetter() == false && listRife[position - 1].title.lowercase()
                        .firstOrNull()
                        ?.isLetter() == true
                } else {
                    listRife[position].title.lowercase()
                        .codePointAt(0) != listRife[position - 1].title.lowercase().codePointAt(0)
                }
            } else return false
        }

        override fun getSectionHeader(position: Int): String {
            if (listRife.isNotEmpty()) {
                val sub = listRife[position].title.uppercase()[0]
                return if (sub.isLetter()) {
                    sub.toString()
                } else {
                    "#"
                }
            } else {
                return ""
            }
        }
    }
}