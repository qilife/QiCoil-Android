package com.Meditation.Sounds.frequencies.utils.extensions

import android.widget.SeekBar
import kotlin.math.max
import kotlin.math.min

fun SeekBar.getCurrent(
    minSeek: Double,
    maxSeek: Double,
    percent: Double = 0.0
): Float {
    val p = max(0.0, min(1.0, percent))
    this.apply {
        progress = (this.max * p).toInt()
    }
    return (minSeek + (maxSeek - minSeek) * (p * this.max) / (this.max)).toFloat()
}

fun SeekBar.getProgress(minSeek: Double, maxSeek: Double, process: Int = 0): Float {
    return (minSeek + (maxSeek - minSeek) * process / (this.max)).toFloat()
}

fun SeekBar.getPercent(minSeek: Double, maxSeek: Double, process: Float = 0F): Int {
    return (((process - minSeek) * this.max) / (maxSeek - minSeek)).toInt()
}