package com.Meditation.Sounds.frequencies.lemeor.data.model

import androidx.room.Entity
import androidx.room.TypeConverters
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.ProgramConverter
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.RifeConverter
import com.google.android.datatransport.runtime.R

data class RifeResponse(
    val message: String,
    @TypeConverters(RifeConverter::class) var data: List<Rife>,
)

data class ProgramsResponse(
    val message: String,
    @TypeConverters(ProgramConverter::class) var data: List<Program>,
)

data class ProgramCreateResponse(
    val message: String,
    @TypeConverters(ProgramConverter::class) var data: Program,
)