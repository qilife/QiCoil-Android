package com.Meditation.Sounds.frequencies.lemeor.ui.videos

import androidx.lifecycle.LiveData
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Playlist

class VideoRepository(private val localData: DataBase) {
    fun getPlaylists(): LiveData<List<Playlist>>? {
        return localData.playlistDao().getPlaylists()
    }
}