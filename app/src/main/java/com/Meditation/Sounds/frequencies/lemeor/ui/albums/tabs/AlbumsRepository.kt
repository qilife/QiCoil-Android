package com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs

import androidx.lifecycle.LiveData
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.*

class AlbumsRepository(private val localData: DataBase) {

    fun getTiers(): LiveData<List<Tier>>? {
        return localData.tierDao().getTiers()
    }

    fun getCategoriesByTierId(tierId: Int): LiveData<List<Category>>? {
        return localData.categoryDao().getCategoriesByTierId(tierId)
    }

    fun getAlbumsByCategory(id: Int): LiveData<List<Album>>? {
        return localData.albumDao().getAlbumsByCategoryLiveData(id)
    }
}