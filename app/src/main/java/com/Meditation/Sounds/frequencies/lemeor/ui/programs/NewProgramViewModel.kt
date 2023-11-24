package com.Meditation.Sounds.frequencies.lemeor.ui.programs

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.Meditation.Sounds.frequencies.lemeor.data.model.*
import com.Meditation.Sounds.frequencies.lemeor.ui.main.UpdateTrack
import retrofit2.Response

class NewProgramViewModel(private val repository: ProgramRepository) : ViewModel() {

    fun getPrograms(isMy: Boolean) : LiveData<List<Program>> {
        return repository.getListProgram()
    }

    suspend fun insert(program: Program?) {
        repository.insert(program)
    }

    suspend fun delete(program: Program?) {
        repository.delete(program)
    }

    suspend fun getTrackById(id: Int): Track? {
        return repository.getTrackById(id)
    }

    suspend fun getAlbumById(id: Int, category_id: Int): Album? {
        return repository.getAlbumById(id, category_id)
    }

    suspend fun createProgram(name: String) = repository.createProgram(name)
    suspend fun deleteProgram(idProgram: String) = repository.deleteProgram(idProgram)

    suspend fun updateTrackToProgram(track: UpdateTrack) = repository.updateTrackToProgram(track)

    suspend fun udpate(program: Program) {
        repository.update(program)
    }



}