package com.Meditation.Sounds.frequencies.generators

abstract class BaseGenerator {
    abstract fun getValue(phase: Double, period: Double): Short
}