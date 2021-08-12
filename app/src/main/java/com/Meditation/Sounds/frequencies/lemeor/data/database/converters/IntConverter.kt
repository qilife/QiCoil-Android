package com.Meditation.Sounds.frequencies.lemeor.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class IntConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun to(value: String?): ArrayList<Int>? {
            val type = object : TypeToken<ArrayList<Int>>() {}.type
            return Gson().fromJson(value, type)
        }

        @TypeConverter
        @JvmStatic
        fun from(list: ArrayList<Int>?): String? {
            val type = object : TypeToken<ArrayList<Int>>() {}.type
            return Gson().toJson(list, type)
        }
    }
}