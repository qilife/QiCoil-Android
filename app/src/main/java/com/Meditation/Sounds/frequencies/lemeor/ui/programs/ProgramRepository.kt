package com.Meditation.Sounds.frequencies.lemeor.ui.programs

import androidx.lifecycle.LiveData
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track

class ProgramRepository(private val localData: DataBase) {

    fun getMy(): LiveData<List<Program>>? {
        return localData.programDao().getMy(true)
    }

    fun getPrograms(): LiveData<List<Program>>? {
        return localData.programDao().getPrograms()
    }

    fun insert(program: Program?) {
        localData.programDao().insert(program)
    }

    fun delete(program: Program?) {
        localData.programDao().delete(program)
    }

    fun getTrackById(id: Int): Track? {
        return localData.trackDao().getTrackById(id)
    }
}