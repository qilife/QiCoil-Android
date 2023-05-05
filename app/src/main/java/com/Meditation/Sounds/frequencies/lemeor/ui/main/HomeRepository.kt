package com.Meditation.Sounds.frequencies.lemeor.ui.main

import com.Meditation.Sounds.frequencies.QApplication
import com.Meditation.Sounds.frequencies.lemeor.*
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.*
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.performGetOperation
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.Meditation.Sounds.frequencies.lemeor.ui.auth.updateUnlocked

class HomeRepository(private val apiHelper: ApiHelper, private val localData: DataBase) {

    suspend fun getProfile(): User {
        return apiHelper.getProfile()
    }

    suspend fun getApkList(): List<String> {
        return apiHelper.getApkList()
    }

    fun getHome(user_id: String) = performGetOperation(
            databaseQuery = { localData.homeDao().getHome() },
            networkCall = { apiHelper.getHome(user_id) },
            saveCallResult = {
                localSave(it)
            }
    )

    fun getAlbumById(id: Int): Album? {
        return localData.albumDao().getAlbumById(id)
    }

    fun searchAlbum(searchString: String): List<Album>? {
        return localData.albumDao().searchAlbum(searchString)
    }

    fun searchTrack(searchString: String): List<Track>? {
        return localData.trackDao().searchTrack(searchString)
    }

    fun searchProgram(searchString: String): List<Program>? {
        return localData.programDao().searchProgram(searchString)
    }

    fun localSave(it: HomeResponse?) {
        val user = PreferenceHelper.getUser(QApplication.getInstance().applicationContext)
        user?.unlocked_tiers = it?.unlocked_tiers.orEmpty()
        user?.unlocked_categories = it?.unlocked_categories.orEmpty()
        user?.unlocked_albums = it?.unlocked_albums.orEmpty()
        PreferenceHelper.saveUser(QApplication.getInstance().applicationContext, user)

        syncTiers(localData, it)
        syncCategories(localData, it)
        syncTags(localData, it)
        syncPrograms(localData, it)
        syncPlaylists(localData, it)
        syncAlbums(localData, it)
        syncTracks(localData, it)

        user?.let { user -> updateUnlocked(QApplication.getInstance().applicationContext, user, true) }
    }
}
