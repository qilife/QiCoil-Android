package com.Meditation.Sounds.frequencies.lemeor.data.database.converters

import androidx.room.TypeConverter
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RifeConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun to(value: String): List<Rife> {
            val type = object : TypeToken<List<Rife>>() {}.type
            return Gson().fromJson(value, type)
        }

        @TypeConverter
        @JvmStatic
        fun from(list: List<Rife>): String {
            val type = object : TypeToken<List<Rife>>() {}.type
            return Gson().toJson(list, type)
        }
    }
}