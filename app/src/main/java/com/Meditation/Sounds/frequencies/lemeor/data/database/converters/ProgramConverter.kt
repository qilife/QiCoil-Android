package com.Meditation.Sounds.frequencies.lemeor.data.database.converters

import androidx.room.TypeConverter
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ProgramConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun to(value: String): List<Program> {
            val type = object : TypeToken<List<Program>>() {}.type
            return Gson().fromJson(value, type)
        }

        @TypeConverter
        @JvmStatic
        fun from(list: List<Program>): String {
            val type = object : TypeToken<List<Program>>() {}.type
            return Gson().toJson(list, type)
        }
    }
}