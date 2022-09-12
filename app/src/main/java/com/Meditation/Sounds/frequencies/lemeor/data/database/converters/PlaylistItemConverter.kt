package com.Meditation.Sounds.frequencies.lemeor.data.database.converters

import androidx.room.TypeConverter
import com.Meditation.Sounds.frequencies.lemeor.data.model.Playlist
import com.Meditation.Sounds.frequencies.models.PlaylistItemSong
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlaylistItemConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun to(value: String): ArrayList<PlaylistItemSong> {
            val type = object : TypeToken<ArrayList<PlaylistItemSong>>() {}.type
            return Gson().fromJson(value, type)
        }

        @TypeConverter
        @JvmStatic
        fun from(list: ArrayList<PlaylistItemSong>): String {
            val type = object : TypeToken<ArrayList<PlaylistItemSong>>() {}.type
            return Gson().toJson(list, type)
        }
    }
}