package com.Meditation.Sounds.frequencies.generators

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.Meditation.Sounds.frequencies.generators.model.WaveTypes
import kotlin.math.max
import kotlin.math.min


class SoundGenerator {
    private var bufferThread: Thread? = null
    private var audioTrack: AudioTrack? = null
    private var generator: SignalDataGenerator? = null
    var isPlaying = false
        private set
    private var minSamplesSize = 0
    private var waveType: WaveTypes = WaveTypes.SINUSOIDAL
    private var rightVolume = 1f
    private var leftVolume = 1f
    fun setAutoUpdateOneCycleSample(autoUpdateOneCycleSample: Boolean) {
        if (generator != null) generator!!.setAutoUpdateOneCycleSample(autoUpdateOneCycleSample)
    }

    var sampleRate: Int
        get() = if (generator != null) generator!!.sampleRate else 0
        set(sampleRate) {
            if (generator != null) generator!!.sampleRate = sampleRate
        }

    fun refreshOneCycleData() {
        if (generator != null) generator!!.createOneCycleData(true)
    }

    var frequency: Float
        get() = if (generator != null) generator!!.getFrequency() else 0F
        set(v) {
            if (generator != null) generator!!.setFrequency(v)
        }

    fun setBalance(balance: Float) {
        val b = max(-1f, min(1f, balance))
        rightVolume = if (b >= 0) 1F else if (b == -1f) 0F else 1F + b
        leftVolume = if (b <= 0) 1F else if (b == 1f) 0F else 1F - b
        if (audioTrack != null) {
            audioTrack!!.setStereoVolume(leftVolume, rightVolume)
        }
    }

    fun setVolume(volume: Float) {
        val v = max(0f, min(1f, volume))
        if (audioTrack != null) {
            audioTrack!!.setStereoVolume(leftVolume * v, rightVolume * v)
        }
    }

    fun setWaveform(waveType: WaveTypes) {
        if (this.waveType == waveType || generator == null) return
        this.waveType = waveType
        when (waveType) {
            WaveTypes.SINUSOIDAL -> generator!!.setGenerator(SinusoidalGenerator())
            WaveTypes.TRIANGLE -> generator!!.setGenerator(TriangleGenerator())
            WaveTypes.SQUAREWAVE -> generator!!.setGenerator(
                SquareWaveGenerator()
            )
            WaveTypes.SAWTOOTH -> generator!!.setGenerator(SawtoothGenerator())
        }
    }

    fun init(sampleRate: Int): Boolean {
        return try {
            minSamplesSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            generator = SignalDataGenerator(minSamplesSize, sampleRate)
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minSamplesSize,
                AudioTrack.MODE_STREAM
            )
            true
        } catch (ex: Exception) {
            false
        }
    }

    fun startPlayback() {
        if (bufferThread != null || audioTrack == null) return
        isPlaying = true
        bufferThread = Thread {
            audioTrack!!.flush()
            audioTrack!!.playbackHeadPosition = 0
            audioTrack!!.play()
            while (isPlaying) {
                audioTrack!!.write(generator!!.data, 0, minSamplesSize)
            }
        }
//        isPlayingStreamHandler.change(true)
        bufferThread!!.start()
    }

    fun stopPlayback() {
        if (bufferThread == null) return
        isPlaying = false
        try {
            bufferThread!!.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
//        isPlayingStreamHandler.change(false)
        bufferThread = null
        if (audioTrack != null) {
            audioTrack!!.stop()
        }
    }

    fun release() {
        if (isPlaying) stopPlayback()
        audioTrack!!.release()
    }
}