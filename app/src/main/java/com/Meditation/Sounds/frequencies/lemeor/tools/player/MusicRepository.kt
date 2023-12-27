package com.Meditation.Sounds.frequencies.lemeor.tools.player

import android.os.Parcelable
import com.Meditation.Sounds.frequencies.lemeor.currentTrack
import com.Meditation.Sounds.frequencies.lemeor.currentTrackIndex
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import kotlinx.android.parcel.Parcelize
import java.util.*

class MusicRepository<T>(private val data: List<T>) {
    private val maxIndex = data.size - 1
    var currentItemIndex = 0

    fun getNext(): T {
        if (currentItemIndex == maxIndex) currentItemIndex = 0 else currentItemIndex++
        return getCurrent()
    }

    fun getPrevious(): T {
        if (currentItemIndex == 0) currentItemIndex = maxIndex else currentItemIndex--
        return getCurrent()
    }

    fun getRandom(): T {
        val random = Random().nextInt(maxIndex + 1)
        if (currentItemIndex != random) {
            currentItemIndex = random
        }
        return getCurrent()
    }

    fun getCurrent(): T {
        if (currentItemIndex <= data.size - 1) {
            currentTrack.value = data[currentItemIndex]
            currentTrackIndex.value = currentItemIndex
            return data[currentItemIndex]
        }
        return data[data.size - 1]
    }

    fun isLastTrack(): Boolean {
        return currentItemIndex == maxIndex
    }

    fun getTime(minutes : Int) = ((data.size - currentItemIndex) * minutes * 60).toLong()

    @Parcelize
    class Track(
        val trackId: Int,
        override var title: String,
        val artist: String,
        val albumId: Int,
        val album: Album,
        override var resId: Int,
        override var duration: Long,
        override var position: Long,
        var filename: String
    ) : Music(title, resId, duration, position), Parcelable {}

    @Parcelize
    open class Music(
        open var title: String,
        open var resId: Int,
        open var duration: Long,
        open var position: Long,
    ) : Parcelable {}

    @Parcelize
    class Frequency(
        val index: Int,
        override var title: String,
        val frequency: Float,
        val rifeId: Int,
        override var resId: Int,
        var isSelected: Boolean,
        override var duration: Long,
        override var position: Long,
    ) : Music(title, resId, duration, position), Parcelable {}
}