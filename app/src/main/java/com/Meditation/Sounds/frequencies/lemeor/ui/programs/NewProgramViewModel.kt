package com.Meditation.Sounds.frequencies.lemeor.ui.programs

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track

class NewProgramViewModel(private val repository: ProgramRepository) : ViewModel() {

    fun getPrograms(isMy: Boolean) : LiveData<List<Program>>? {
        return if (isMy) {
            repository.getMy()
        } else {
            repository.getPrograms()
        }
    }

    fun insert(program: Program?) {
        repository.insert(program)
    }

    fun delete(program: Program?) {
        repository.delete(program)
    }

    fun getTrackById(id: Int): Track? {
        return repository.getTrackById(id)
    }
}