package com.Meditation.Sounds.frequencies.generators

import kotlin.math.sin

class SinusoidalGenerator : BaseGenerator() {
    override fun getValue(phase: Double, period: Double): Short {
        return (Short.MAX_VALUE * sin(phase)).toInt().toShort()
    }
}