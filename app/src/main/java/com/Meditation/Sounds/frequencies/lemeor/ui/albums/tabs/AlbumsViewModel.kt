package com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.Meditation.Sounds.frequencies.lemeor.data.model.*
import com.Meditation.Sounds.frequencies.lemeor.data.utils.Resource
import com.Meditation.Sounds.frequencies.lemeor.data.utils.getErrorMsg
import com.Meditation.Sounds.frequencies.tasks.CheckFreeAlbum
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException

class AlbumsViewModel(private val repository: AlbumsRepository) : ViewModel() {

    val tiers = repository.getTiers()

    fun categoriesByTierId(id: Int): LiveData<List<Category>>? {
        return repository.getCategoriesByTierId(id)
    }

    fun albums(id: Int): LiveData<List<Album>>? {
        return repository.getAlbumsByCategory(id)
    }

    fun CheckFreeAlbum(
            user_id: String,
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = repository.checkAlbum(user_id)))
        } catch (exception: HttpException) {
            emit(Resource.error(data = null, message = getErrorMsg(exception)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun SaveFreeAlbum(
            user_id: String,
            album_id: String,
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = repository.SaveAlbum(user_id,album_id)))
        } catch (exception: HttpException) {
            emit(Resource.error(data = null, message = getErrorMsg(exception)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
}