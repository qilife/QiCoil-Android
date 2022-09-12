package com.Meditation.Sounds.frequencies.lemeor.data.database.converters

import androidx.room.TypeConverter
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AlbumsConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun to(value: String): List<Album> {
            val type = object : TypeToken<List<Album>>() {}.type
            return Gson().fromJson(value, type)
        }

        @TypeConverter
        @JvmStatic
        fun from(list: List<Album>): String {
            val type = object : TypeToken<List<Album>>() {}.type
            return Gson().toJson(list, type)
        }
    }
}