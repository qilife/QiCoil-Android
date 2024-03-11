package com.Meditation.Sounds.frequencies.lemeor.ui.rife.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.utils.CombinedLiveData
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.utils.extensions.getCurrent
import com.Meditation.Sounds.frequencies.utils.extensions.getPercent
import com.Meditation.Sounds.frequencies.utils.extensions.getProgress
import com.Meditation.Sounds.frequencies.views.ItemOffsetRightDecoration
import kotlinx.android.synthetic.main.fragment_frequency_page.btnAdd
import kotlinx.android.synthetic.main.fragment_frequency_page.btnMinus
import kotlinx.android.synthetic.main.fragment_frequency_page.btnPlay
import kotlinx.android.synthetic.main.fragment_frequency_page.rcvOptions
import kotlinx.android.synthetic.main.fragment_frequency_page.sbHz
import kotlinx.android.synthetic.main.fragment_frequency_page.sbTune
import kotlinx.android.synthetic.main.fragment_frequency_page.tvCurrent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class FrequencyPageFragment : Fragment() {
    private val mFrequencyAdapter = FrequencyAdapter { i ->
        minValue = Constants.optionsHz[i].first
        maxValue = Constants.optionsHz[i].second
        viewModel.updateHz(sbHz.getCurrent(minValue, maxValue, 0.5))
        viewModel.updateTune(resetTune())
    }
    var lastValidProgress = 0
    var minValue = Constants.optionsHz[0].first
    var maxValue = Constants.optionsHz[0].second

    private lateinit var viewModel: FrequencyViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Any?) {
        if (event is String && event == "play Album") {
            viewModel.stopAlways()
            btnPlay.text = getString(R.string.play)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_frequency_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        onSubscriber()
        onObserve()
    }

    private fun onObserve() {
        viewModel.apply {
            CombinedLiveData(hz, tune, combine = { hz, tune ->
                (hz ?: 0).toFloat() + (tune ?: 0).toFloat()
            }).observe(viewLifecycleOwner) { current ->
                viewModel.swipeTune = current in minValue..maxValue
                if (current in minValue..maxValue) {
                    updateCurrent(current)
                    setFrequency(current)
                    tvCurrent.text = String.format("%.2f", current)
                } else if (current <= minValue) {
                    updateCurrent(minValue.toFloat())
                    setFrequency(minValue.toFloat())
                    tvCurrent.text = String.format("%.2f", minValue)
                } else if (current >= maxValue) {
                    updateCurrent(maxValue.toFloat())
                    setFrequency(maxValue.toFloat())
                    tvCurrent.text = String.format("%.2f", maxValue)
                }
            }
        }
    }

    private fun onSubscriber() {
        sbHz.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar, p1: Int, p2: Boolean) {
                viewModel.apply {
                    updateHz(sbHz.getProgress(minValue, maxValue, p1))
                    updateTune(resetTune())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        sbTune.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, p2: Boolean) {
                viewModel.apply {
                    lastValidProgress = if (swipeTune) {
                        progress
                    } else {
                        updateTune(resetTune())
                        sbTune.progress
                    }
                    updateTune(sbTune.getProgress(-5.0, 5.0, progress))
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        btnPlay.setOnClickListener {
            EventBus.getDefault().post("play Rife")
            btnPlay.text =
                if (viewModel.playOrStop()) getString(R.string.tv_stop) else getString(R.string.play)
        }

        btnMinus.setOnClickListener {
            viewModel.apply {
                val lf = roundUpToNearestMultiple(current.value ?: 0F, -5, Pair(minValue, maxValue))
                sbHz.progress = sbHz.getPercent(minValue, maxValue, lf)
            }
        }
        btnAdd.setOnClickListener {
            viewModel.apply {
                val lf = roundUpToNearestMultiple(current.value ?: 0F, 5, Pair(minValue, maxValue))
                sbHz.progress = sbHz.getPercent(minValue, maxValue, lf)
            }
        }
    }

    private fun resetTune() = sbTune.getCurrent(-5.0, 5.0, 0.5)
    private fun initView() {
        viewModel = ViewModelProviders.of(this)[FrequencyViewModel::class.java]

        val itemDecoration = ItemOffsetRightDecoration(
            requireContext(),
            if (Utils.isTablet(context)) R.dimen.item_offset else R.dimen.margin_buttons
        )
        rcvOptions.apply {
            adapter = mFrequencyAdapter
            addItemDecoration(itemDecoration)
            itemAnimator = null
        }
        mFrequencyAdapter.setCategories(Constants.textHz)
        sbHz.getCurrent(minValue, maxValue, 0.5)
        resetTune()
        lastValidProgress = sbTune.progress
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopAlways()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.soundRelease()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        @JvmStatic
        fun newInstance(): FrequencyPageFragment {
            return FrequencyPageFragment().apply {}
        }
    }
}


