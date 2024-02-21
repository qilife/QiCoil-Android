package com.Meditation.Sounds.frequencies.lemeor.ui.rife

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.Meditation.Sounds.frequencies.lemeor.hideKeyboard
import com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail.NewAlbumDetailFragment
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.tabs.ProgramAdapter
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.views.ItemOffsetBottomDecoration
import kotlinx.android.synthetic.main.fragment_search_rife.*


class SearchRifeFragment : Fragment() {
    private lateinit var mViewModel: NewRifeViewModel
    private val mProgramAdapter = ProgramAdapter {
        closeSearch()
        openAlbum(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_rife, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        onObserve()
        rife_search?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isNotEmpty()) {
                    rife_search_clear.visibility = View.VISIBLE
                    mViewModel.search(s.toString().trim())
                } else {
                    rife_search_clear.visibility = View.GONE
                    mViewModel.search("")
                }
            }
        })
        rife_search_clear.setOnClickListener {
            closeSearch()
            mViewModel.search("")
        }
        btnBack.setOnClickListener { onBackPressed() }
        val itemDecoration = ItemOffsetBottomDecoration(
            requireContext(),
            if (Utils.isTablet(context)) R.dimen.item_offset else R.dimen.margin_buttons
        )
        rcvSearch.apply {
            adapter = mProgramAdapter
            addItemDecoration(itemDecoration)
        }
        mViewModel.getRifeLocal{ data->
            val list  = data.toMutableList().sortedWith(compareBy<Rife> {
            when {
                it.title.lowercase().firstOrNull()?.isLetter() == true -> 0
                else -> 1
            }
        }.thenBy { it.title.lowercase() })
            mProgramAdapter.setListRife(list)
        }
    }

    private fun openAlbum(rife: Rife) {
        parentFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.trans_right_to_left_in, R.anim.trans_right_to_left_out,
                R.anim.trans_left_to_right_in, R.anim.trans_left_to_right_out
            )
            .replace(
                R.id.nav_host_fragment,
                NewAlbumDetailFragment.newInstance(0, 0, Constants.TYPE_RIFE, rife),
                NewAlbumDetailFragment().javaClass.simpleName
            )
            .commit()
    }

    private fun onObserve() {
        mViewModel.result.observe(viewLifecycleOwner) { listRift ->
            mProgramAdapter.setListRife(listRift)
        }
    }

    private fun initView() {
        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[NewRifeViewModel::class.java]
    }

    private fun onBackPressed() {
        closeSearch()
        val fragment = NewRifeFragment()

        parentFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.trans_left_to_right_in, R.anim.trans_left_to_right_out)
            .replace(R.id.nav_host_fragment, fragment, fragment.javaClass.simpleName)
            .commit()
    }

    companion object {
        const val ARG_PROGRAM_ID = "arg_rife"

        @JvmStatic
        fun newInstance() = SearchRifeFragment().apply {
        }
    }

    private fun closeSearch() {
        rife_search.text = null
        hideKeyboard(requireContext(), rife_search)
        rife_search.clearFocus()
    }

    private fun clearSearch() {
//        lblheaderprograms.visibility = View.GONE
//        lblheaderfrequencies.visibility = View.GONE
//        lblheaderalbums.visibility = View.GONE
//        lblnoresult.visibility = View.VISIBLE
    }
}