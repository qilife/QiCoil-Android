package com.Meditation.Sounds.frequencies.lemeor.tools.downloader

import androidx.lifecycle.LiveData
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album

class DownloaderRepository(private val localData: DataBase) {
    fun getAlbumsById(id: Int, category_id: Int): LiveData<Album>? {
        return localData.albumDao().getAlbumsById(id, category_id)
    }

    suspend fun isAlbumDownloaded(isDownloaded: Boolean, id: Int) {
        localData.albumDao().isAlbumDownloaded(isDownloaded, id)
    }

    suspend fun isTrackDownloaded(isDownloaded: Boolean, id: Int) {
        localData.trackDao().isTrackDownloaded(isDownloaded, id)
    }
}