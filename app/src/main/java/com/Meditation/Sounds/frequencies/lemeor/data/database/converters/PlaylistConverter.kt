package com.Meditation.Sounds.frequencies.lemeor.data.database.converters

import androidx.room.TypeConverter
import com.Meditation.Sounds.frequencies.lemeor.data.model.Playlist
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlaylistConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun to(value: String): List<Playlist> {
            val type = object : TypeToken<List<Playlist>>() {}.type
            return Gson().fromJson(value, type)
        }

        @TypeConverter
        @JvmStatic
        fun from(list: List<Playlist>): String {
            val type = object : TypeToken<List<Playlist>>() {}.type
            return Gson().toJson(list, type)
        }
    }
}