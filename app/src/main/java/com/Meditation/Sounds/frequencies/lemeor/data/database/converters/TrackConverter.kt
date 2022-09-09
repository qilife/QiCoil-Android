package com.Meditation.Sounds.frequencies.lemeor.data.database.converters

import androidx.room.TypeConverter
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TrackConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun to(value: String): List<Track> {
            val type = object : TypeToken<List<Track>>() {}.type
            return Gson().fromJson(value, type)
        }

        @TypeConverter
        @JvmStatic
        fun from(list: List<Track>): String {
            val type = object : TypeToken<List<Track>>() {}.type
            return Gson().toJson(list, type)
        }
    }
}