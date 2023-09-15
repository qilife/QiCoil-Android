package com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail

import androidx.lifecycle.LiveData
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album

class AlbumDetailRepository(private val localData: DataBase) {
    fun getAlbumsById(id: Int, category_id: Int): LiveData<Album>? {
        return localData.albumDao().getAlbumsById(id, category_id)
    }

    suspend fun getAlbumsByIdOnce(id: Int, category_id: Int): Album? {
        return localData.albumDao().getAlbumsByIdOnce(id, category_id)
    }
}

