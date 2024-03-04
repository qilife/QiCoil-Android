package com.Meditation.Sounds.frequencies.lemeor.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StringConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun to(value: String?): List<String>? {
            val type = object : TypeToken<List<String>>() {}.type
            return Gson().fromJson(value, type)
        }

        @TypeConverter
        @JvmStatic
        fun from(list: List<String>?): String? {
            val type = object : TypeToken<List<String>>() {}.type
            return Gson().toJson(list, type)
        }
    }
}

class StringArrConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun to(value: String?): ArrayList<String>? {
            val type = object : TypeToken<ArrayList<String>>() {}.type
            return Gson().fromJson(value, type)
        }

        @TypeConverter
        @JvmStatic
        fun from(list: ArrayList<String>?): String? {
            val type = object : TypeToken<ArrayList<String>>() {}.type
            return Gson().toJson(list, type)
        }
    }
}