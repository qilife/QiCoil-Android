package com.Meditation.Sounds.frequencies.lemeor.ui.programs

import androidx.lifecycle.LiveData
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.*
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.ui.main.UpdateTrack
import retrofit2.Response

class ProgramRepository(private val localData: DataBase, private val apiHelper: ApiHelper) {

    fun getMy(): LiveData<List<Program>> {
        return localData.programDao().getMy(true)
    }

    fun getPrograms(): LiveData<List<Program>> {
        return localData.programDao().getPrograms()
    }

    suspend fun insert(program: Program?) {
        localData.programDao().insert(program)
    }

    suspend fun delete(program: Program?) {
        localData.programDao().delete(program)
    }

    suspend fun getTrackById(id: Int): Track? {
        return localData.trackDao().getTrackById(id)
    }

    suspend fun getAlbumById(id: Int, category_id: Int): Album? {
        return localData.albumDao().getAlbumById(id, category_id)
    }

    suspend fun createProgram(name: String) = apiHelper.createPrograms(name)
    suspend fun deleteProgram(idProgram: String) = apiHelper.deleteProgram(idProgram)
    suspend fun updateTrackToProgram(track: UpdateTrack) = apiHelper.updateTrackToProgram(track)
    suspend fun getProgramsRemote() = apiHelper.getPrograms()
    suspend fun update(program: Program) {
        localData.programDao().update(program.id,program.deleted)
    }
    fun getListProgram() : LiveData<List<Program>>{
        return localData.programDao().getListProgram()
    }

    fun getListTrack() : LiveData<List<Track>>{
        return localData.trackDao().getTracks()
    }

    suspend fun getProgramById(id:Int) = localData.programDao().getProgramById(id)
    suspend fun updateProgram(program: Program) = localData.programDao().updateProgram(program)
}