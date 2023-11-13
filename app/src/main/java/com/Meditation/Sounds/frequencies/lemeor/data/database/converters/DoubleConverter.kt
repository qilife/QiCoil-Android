package com.Meditation.Sounds.frequencies.lemeor.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DoubleConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun to(value: String?): ArrayList<Double>? {
            val type = object : TypeToken<ArrayList<Double>>() {}.type
            return Gson().fromJson(value, type)
        }

        @TypeConverter
        @JvmStatic
        fun from(list: ArrayList<Double>?): String? {
            val type = object : TypeToken<ArrayList<Double>>() {}.type
            return Gson().toJson(list, type)
        }
    }
}