package com.Meditation.Sounds.frequencies.lemeor.data.database.converters

import androidx.room.TypeConverter
import com.Meditation.Sounds.frequencies.lemeor.data.model.Tier
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TierConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun to(value: String): List<Tier> {
            val type = object : TypeToken<List<Tier>>() {}.type
            return Gson().fromJson(value, type)
        }

        @TypeConverter
        @JvmStatic
        fun from(list: List<Tier>): String {
            val type = object : TypeToken<List<Tier>>() {}.type
            return Gson().toJson(list, type)
        }
    }
}