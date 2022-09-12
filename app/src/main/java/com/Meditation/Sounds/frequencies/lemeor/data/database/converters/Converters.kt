package com.Meditation.Sounds.frequencies.lemeor.data.database.converters

import androidx.room.TypeConverter
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun toAlbumJson(value: Album?): String? {
        return if (value != null) Gson().toJson(value) else null
    }

    @TypeConverter
    fun fromAlbumJson(album: String?): Album? {
        return if (album?.isNotEmpty() == true) {
            Gson().fromJson(album, Album::class.java)
        } else {
            null
        }
    }
}