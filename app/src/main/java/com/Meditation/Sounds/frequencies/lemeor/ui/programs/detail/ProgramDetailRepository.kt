package com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail

import androidx.lifecycle.LiveData
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProgramDetailRepository(private val localData: DataBase) {
    fun getProgramById(id: Int): LiveData<Program> {
        return localData.programDao().getProgramByIdLive(id)
    }

    suspend fun getTrackById(id: Int): Track? {
        return localData.trackDao().getTrackById(id)
    }

    suspend fun getAlbumById(id: Int, categoryId: Int): Album? {
        return localData.albumDao().getAlbumById(id,categoryId)
    }

    fun insertRife(rife: Rife) {
        CoroutineScope(Dispatchers.IO).launch {
            localData.rifeDao().insert(rife)
        }
    }
}