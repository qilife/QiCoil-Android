package com.Meditation.Sounds.frequencies.lemeor.data.model

import androidx.room.Entity
import androidx.room.TypeConverters
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.AlbumsConverter

@Entity(tableName = "list_rife")
data class RifeResponse(
    @TypeConverters(AlbumsConverter::class) var albums: List<Album>
)