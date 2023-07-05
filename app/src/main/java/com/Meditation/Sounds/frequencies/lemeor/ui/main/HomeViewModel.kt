package com.Meditation.Sounds.frequencies.lemeor.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.Meditation.Sounds.frequencies.lemeor.data.model.*
import com.Meditation.Sounds.frequencies.lemeor.data.utils.Resource
import com.Meditation.Sounds.frequencies.lemeor.data.utils.getErrorMsg
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.HttpException

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {
    //val home = repository.getHome(user_id)

    fun getHome(id: String): LiveData<Resource<HomeResponse>> {
        return repository.getHome(id)
    }


    fun getProfile( ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = repository.getProfile()))
        } catch (exception: HttpException) {
            emit(Resource.error(data = null, message = getErrorMsg(exception)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    suspend fun getApkList(): List<String> {
        return repository.getApkList()
    }

    suspend fun reportTrack(trackId: Int, trackUrl: String): Status {
        return repository.reportTrack(trackId, trackUrl)
    }

    fun getAlbumById(id: Int): Album? {
        return repository.getAlbumById(id)
    }

    fun searchAlbum(searchString: String): List<Album>? {
        return repository.searchAlbum(searchString)
    }

    fun searchTrack(searchString: String): List<Track>? {
        return repository.searchTrack(searchString)
    }

    fun searchProgram(searchString: String): List<Program>? {
        return repository.searchProgram(searchString)
    }

    fun loadFromCache(context: Context) {
        val cache: HomeResponse = Gson().fromJson(
                context.assets.open("db_caсhe.json").bufferedReader().use {
                    reader -> reader.readText()
                },
                HomeResponse::class.java
        )

        GlobalScope.launch { repository.localSave(cache) }
    }

    fun loadDataLastHomeResponse(context: Context){
        val homeResponse = PreferenceHelper.getLastHomeResponse(context)
        if (homeResponse?.tiers != null && homeResponse.tiers.isNotEmpty()) {
            GlobalScope.launch { repository.localSave(homeResponse) }
        }
    }
}