package com.Meditation.Sounds.frequencies.lemeor.ui.rife.tabs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.Meditation.Sounds.frequencies.generators.SoundGenerator
import com.Meditation.Sounds.frequencies.generators.model.WaveTypes
import com.Meditation.Sounds.frequencies.utils.Constants

class FrequencyViewModel : ViewModel() {
    private val _current =
        MutableLiveData((Constants.optionsHz[0].second - Constants.optionsHz[0].first).toFloat() / 2)
    val current: LiveData<Float> = _current

    private val _hz =
        MutableLiveData((Constants.optionsHz[0].second - Constants.optionsHz[0].first).toFloat() / 2)
    val hz: LiveData<Float> = _hz

    private val _tune =
        MutableLiveData(0F)
    val tune: LiveData<Float> = _tune

    var swipeTune = true

    private val soundFrequency = SoundGenerator().apply {
        init(44100)
        setWaveform(WaveTypes.SINUSOIDAL)
        setVolume(1F)
        setBalance(1F)
//        setAutoUpdateOneCycleSample(true)
//        refreshOneCycleData()
    }

    fun updateCurrent(v: Float) {
        _current.postValue(v)
    }

    fun updateHz(v: Float) {
        _hz.postValue(v)
    }

    fun updateTune(v: Float) {
        _tune.postValue(v)
    }

    fun setFrequency(v: Float) {
        soundFrequency.frequency = v
    }

    fun playOrStop(): Boolean {
        if (soundFrequency.isPlaying) {
            soundFrequency.stopPlayback()
        } else {
            soundFrequency.startPlayback()
        }
        return soundFrequency.isPlaying
    }

    fun stopAlways() {
        if (soundFrequency.isPlaying) {
            soundFrequency.stopPlayback()
        }
    }

    fun soundRelease() {
        soundFrequency.release()
    }

    fun roundUpToNearestMultiple(value: Float, num: Int, list: Pair<Double, Double>): Float {
        var valueNew = (value + num).toDouble()
        val remainder = valueNew % num

        if (remainder > 0) {
            valueNew += (num - remainder);
        }

        if (valueNew < list.first) {
            valueNew = list.first
        }

        if (valueNew > list.second) {
            valueNew = list.second
        }

        return valueNew.toFloat()
    }

}