package com.Meditation.Sounds.frequencies.lemeor.ui.main

import com.Meditation.Sounds.frequencies.QApplication
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.HomeResponse
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Status
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.model.User
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.performGetOperation
import com.Meditation.Sounds.frequencies.lemeor.syncAlbums
import com.Meditation.Sounds.frequencies.lemeor.syncCategories
import com.Meditation.Sounds.frequencies.lemeor.syncPlaylists
import com.Meditation.Sounds.frequencies.lemeor.syncPrograms
import com.Meditation.Sounds.frequencies.lemeor.syncRife
import com.Meditation.Sounds.frequencies.lemeor.syncTags
import com.Meditation.Sounds.frequencies.lemeor.syncTiers
import com.Meditation.Sounds.frequencies.lemeor.syncTracks
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.Meditation.Sounds.frequencies.lemeor.ui.auth.updateUnlocked
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeRepository(private val apiHelper: ApiHelper, private val localData: DataBase) {

    suspend fun getProfile(): User {
        return apiHelper.getProfile()
    }

    suspend fun getApkList(): List<String> {
        return apiHelper.getApkList()
    }

    suspend fun reportTrack(trackId: Int, trackUrl: String): Status {
        return apiHelper.reportTrack(trackId, trackUrl)
    }

    fun getHome(user_id: String) = performGetOperation(
        databaseQuery = { localData.homeDao().getHome() },
        networkCall = {
            val data = apiHelper.getHome(user_id)
            data
        },
        saveCallResult = {
            CoroutineScope(Dispatchers.IO).launch {
                localSave(it)
            }
        }
    )

    suspend fun getAlbumById(id: Int, category_id: Int): Album? {
        return localData.albumDao().getAlbumById(id, category_id)
    }

    suspend fun searchAlbum(searchString: String): List<Album> {
        return localData.albumDao().searchAlbum(searchString)
    }

    suspend fun searchTrack(searchString: String): List<Track> {
        return localData.trackDao().searchTrack(searchString)
    }

    suspend fun searchProgram(searchString: String): List<Program> {
        return localData.programDao().searchProgram(searchString)
    }

    suspend fun localSave(it: HomeResponse?) {
        if (it?.tiers != null && it.tiers.isNotEmpty()) {
            PreferenceHelper.saveLastHomeResponse(QApplication.getInstance().applicationContext, it)
        }
        val user = PreferenceHelper.getUser(QApplication.getInstance().applicationContext)
        user?.unlocked_tiers = it?.unlocked_tiers.orEmpty()
        user?.unlocked_categories = it?.unlocked_categories.orEmpty()
        user?.unlocked_albums = it?.unlocked_albums.orEmpty()
        PreferenceHelper.saveUser(QApplication.getInstance().applicationContext, user)

        syncTiers(localData, it)
        syncCategories(localData, it)
        syncTags(localData, it)
        syncPrograms(localData, it, user)
        syncPlaylists(localData, it)
        syncAlbums(localData, it)
        syncTracks(localData, it)

        user?.let {
            updateUnlocked(
                QApplication.getInstance().applicationContext,
                it,
                true
            )
        }
    }


    fun getRife() = performGetOperation(
        databaseQuery = {
            localData.rifeDao().getLiveDataRifes()

        },
        networkCall = {
            val data = apiHelper.getRife()
            data
        },
        saveCallResult = {
            CoroutineScope(Dispatchers.IO).launch {
                syncRife(localData, it)
            }
        }
    )

    suspend fun deleteProgram(id: String) = apiHelper.deleteProgram(id)
    suspend fun syncProgramsApi(listProgram: List<Update>) =
        apiHelper.syncProgramsToServer(listProgram)

    fun getListAlbum() = localData.albumDao().getLiveData()

    fun getListTrack() = localData.trackDao().getTracks()
    fun getListRife() = localData.rifeDao().getLiveDataRifes()

    suspend fun getListA() = localData.albumDao().getAllAlbums()
    suspend fun getListT() = localData.trackDao().getData()
    suspend fun getListR() = localData.rifeDao().getData()
}
