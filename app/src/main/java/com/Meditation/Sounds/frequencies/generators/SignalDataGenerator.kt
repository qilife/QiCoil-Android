package com.Meditation.Sounds.frequencies.generators

import kotlin.math.roundToInt

class SignalDataGenerator(private val bufferSamplesSize: Int, sampleRate: Int) {
    private val _2Pi: Double = 2.0 * Math.PI.toFloat()
    var sampleRate = 48000
        set(sampleRate) {
            field = sampleRate
            phCoefficient = _2Pi / sampleRate.toFloat()
            smoothStep = 1f / sampleRate.toFloat() * 20f
        }
    private var phCoefficient = _2Pi / sampleRate.toFloat()
    private var smoothStep = 1f / sampleRate.toFloat() * 20f
    private var frequency = 50f
    private var generator: BaseGenerator? = SinusoidalGenerator()
    private val backgroundBuffer: ShortArray = ShortArray(bufferSamplesSize)
    private val buffer: ShortArray = ShortArray(bufferSamplesSize)
    private val oneCycleBuffer: MutableList<Int> = ArrayList()
    private var ph: Double = 0.0
    private var oldFrequency = 50f
    private var creatingNewData = false
    private var autoUpdateOneCycleSample = false

    fun isAutoUpdateOneCycleSample(): Boolean {
        return autoUpdateOneCycleSample
    }

    fun setAutoUpdateOneCycleSample(autoUpdateOneCycleSample: Boolean) {
        this.autoUpdateOneCycleSample = autoUpdateOneCycleSample
    }

    fun getGenerator(): BaseGenerator? {
        return generator
    }

    fun setGenerator(generator: BaseGenerator?) {
        this.generator = generator
        createOneCycleData()
    }

    fun getFrequency(): Float {
        return frequency
    }

    fun setFrequency(frequency: Float) {
        this.frequency = frequency
        createOneCycleData()
    }

    init {
        this.sampleRate = sampleRate
        updateData()
        createOneCycleData()
    }

    private fun updateData() {
        creatingNewData = true
        for (i in 0 until bufferSamplesSize) {
            oldFrequency += (frequency - oldFrequency) * smoothStep
            backgroundBuffer[i] = generator!!.getValue(ph, _2Pi)
            ph += oldFrequency * phCoefficient

            //performance of this block is higher than ph %= _2Pi;
            // ifBlock  Test score =  2,470ns
            // ModBlock Test score = 27,025ns
            if (ph > _2Pi) {
                ph -= _2Pi
            }
        }
        creatingNewData = false
    }

    val data: ShortArray
        get() {
            if (!creatingNewData) {
                System.arraycopy(backgroundBuffer, 0, buffer, 0, bufferSamplesSize)
                Thread { updateData() }.start()
            }
            return buffer
        }

    @JvmOverloads
    fun createOneCycleData(force: Boolean = false) {
        if (generator == null || !autoUpdateOneCycleSample && !force) return
        val size = (_2Pi / (frequency * phCoefficient)).roundToInt()
        oneCycleBuffer.clear()
        for (i in 0 until size) {
            oneCycleBuffer.add(
                generator!!.getValue(
                    frequency * phCoefficient * i.toFloat(),
                    _2Pi
                ).toInt()
            )
        }
        oneCycleBuffer.add(generator!!.getValue(0.0, _2Pi).toInt())
//        getOneCycleDataHandler.setData(oneCycleBuffer)
    }
}