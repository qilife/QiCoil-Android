package com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs

import androidx.lifecycle.LiveData
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.*
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper

class AlbumsRepository(private val apiHelper: ApiHelper, private val localData: DataBase) {

    fun getTiers(): LiveData<List<Tier>>? {
        return localData.tierDao().getTiers()
    }

    fun getCategoriesByTierId(tierId: Int): LiveData<List<Category>>? {
        return localData.categoryDao().getCategoriesByTierId(tierId)
    }

    fun getAlbumsByCategory(id: Int): LiveData<List<Album>>? {
        return localData.albumDao().getAlbumsByCategoryLiveData(id)
    }

    suspend fun checkAlbum(
            user_id: String
    ) = apiHelper.getCHeckHome(user_id)

    suspend fun SaveAlbum(
            user_id: String,
            album_id: String
    ) = apiHelper.SaveAlbum(user_id,album_id)
}