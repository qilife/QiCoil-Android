package com.Meditation.Sounds.frequencies.lemeor.ui.rife

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.hideKeyboard
import com.Meditation.Sounds.frequencies.lemeor.ui.rife.tabs.ProgramAdapter
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.views.ItemOffsetBottomDecoration
import kotlinx.android.synthetic.main.fragment_search_rife.*


class SearchRifeFragment : Fragment() {
    private val mProgramAdapter = ProgramAdapter{}
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

        rife_search?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isNotEmpty()) {
                    rife_search_clear.visibility = View.VISIBLE
                } else {
                    rife_search_clear.visibility = View.GONE

                }
            }
        })
        rife_search_clear.setOnClickListener {
            closeSearch()
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
        mProgramAdapter.setListRife(listTest)
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