package com.Meditation.Sounds.frequencies.lemeor.ui.purchase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.adapters.TitleAdapter
import kotlinx.android.synthetic.main.fragment_abundance_1.*
import java.util.ArrayList

class Abundance1Fragment : Fragment() {

    private var titleAdapter: TitleAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_abundance_1, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val titleList = ArrayList<String>()
        titleList.add(getString(R.string.tv_title_advanced_1))
        titleList.add(getString(R.string.tv_title_advanced_2))
        titleList.add(getString(R.string.tv_title_advanced_3))
        titleList.add(getString(R.string.tv_title_advanced_4))
        titleList.add(getString(R.string.tv_title_advanced_5))
        titleList.add(getString(R.string.tv_title_advanced_7))
        titleList.add(getString(R.string.tv_title_advanced_9))
        titleList.add(getString(R.string.tv_title_advanced_10))

        titleAdapter = TitleAdapter(titleList)
        abundanceI_recycler_view.adapter = titleAdapter
    }
}