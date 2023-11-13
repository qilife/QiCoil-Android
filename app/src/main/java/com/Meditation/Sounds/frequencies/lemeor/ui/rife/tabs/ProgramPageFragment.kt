package com.Meditation.Sounds.frequencies.lemeor.ui.rife.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.NewAlbumDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.NewRifeFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.NewRifeViewModel
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.listTest
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.views.ItemOffsetBottomDecoration
import kotlinx.android.synthetic.main.fragment_program_page.*

class ProgramPageFragment : Fragment() {
    private lateinit var mViewModel: NewRifeViewModel
    private var rifeId: Int? = null
    var mainFm: NewRifeFragment? = null
    private val mProgramAdapter = ProgramAdapter {
        if(mainFm != null){
            mainFm!!.openAlbum(it)
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
            this,
            ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        ).get(NewRifeViewModel::class.java)
        val itemDecoration = ItemOffsetBottomDecoration(
            requireContext(),
            if (Utils.isTablet(context)) R.dimen.item_offset else R.dimen.margin_buttons
        )
        rcvProgram.apply {
            adapter = mProgramAdapter
            addItemDecoration(itemDecoration)
        }
        mViewModel.getRifeList().observe(viewLifecycleOwner){
            mProgramAdapter.setListRife(it)
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
}