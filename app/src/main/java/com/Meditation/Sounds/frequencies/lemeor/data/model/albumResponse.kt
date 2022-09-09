package com.Meditation.Sounds.frequencies.lemeor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.*

@Entity(tableName = "albums")
data class albumResponse(
        @TypeConverters(AlbumConverter::class) var albums: List<Album>
)