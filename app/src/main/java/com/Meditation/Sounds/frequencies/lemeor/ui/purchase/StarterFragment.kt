package com.Meditation.Sounds.frequencies.lemeor.ui.purchase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.adapters.TitleAdapter
import kotlinx.android.synthetic.main.fragment_starter.*
import java.util.ArrayList

class StarterFragment : Fragment() {

    private var titleAdapter: TitleAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_starter, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val titleList = ArrayList<String>()
        titleList.add(getString(R.string.tv_title_free_1))
        titleList.add(getString(R.string.tv_title_free_2))
        titleList.add(getString(R.string.tv_title_free_1))
        titleList.add(getString(R.string.tv_title_free_4))

        titleAdapter = TitleAdapter(titleList)
        starter_recycler_view.adapter = titleAdapter
    }
}