package com.Meditation.Sounds.frequencies.lemeor.data.database.converters

import androidx.room.TypeConverter
import com.Meditation.Sounds.frequencies.lemeor.data.model.Category
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CategoryConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun to(value: String): List<Category> {
            val type = object : TypeToken<List<Category>>() {}.type
            return Gson().fromJson(value, type)
        }

        @TypeConverter
        @JvmStatic
        fun from(list: List<Category>): String {
            val type = object : TypeToken<List<Category>>() {}.type
            return Gson().toJson(list, type)
        }
    }
}