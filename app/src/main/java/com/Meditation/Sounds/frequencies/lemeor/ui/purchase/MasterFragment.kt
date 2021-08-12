package com.Meditation.Sounds.frequencies.lemeor.ui.purchase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.adapters.TitleAdapter
import kotlinx.android.synthetic.main.fragment_master.*
import java.util.ArrayList

class MasterFragment : Fragment() {

    private var titleAdapter: TitleAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_master, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val titleList = ArrayList<String>()
        titleList.add(getString(R.string.tv_title_basic_1))
        titleList.add(getString(R.string.tv_title_basic_2))
        titleList.add(getString(R.string.tv_title_basic_3))
        titleList.add(getString(R.string.tv_title_basic_4))
        titleList.add(getString(R.string.tv_title_basic_7))
        titleList.add(getString(R.string.tv_title_basic_8))
        titleList.add(getString(R.string.tv_title_basic_9))

        titleAdapter = TitleAdapter(titleList)
        master_recycler_view.adapter = titleAdapter
    }
}