package com.Meditation.Sounds.frequencies.lemeor.data.database.converters

import androidx.room.TypeConverter
import com.Meditation.Sounds.frequencies.lemeor.data.model.Tag
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TagConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun to(value: String): List<Tag> {
            val type = object : TypeToken<List<Tag>>() {}.type
            return Gson().fromJson(value, type)
        }

        @TypeConverter
        @JvmStatic
        fun from(list: List<Tag>): String {
            val type = object : TypeToken<List<Tag>>() {}.type
            return Gson().toJson(list, type)
        }
    }
}