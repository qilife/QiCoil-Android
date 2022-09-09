package com.Meditation.Sounds.frequencies.lemeor.ui.options.change_pass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.Meditation.Sounds.frequencies.lemeor.data.utils.Resource
import com.Meditation.Sounds.frequencies.lemeor.data.utils.getErrorMsg
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException

class ChangePassViewModel(private val repository: ChangePassRepository) : ViewModel() {

    fun updateProfile(
            email: String,
            password_old: String,
            password: String,
            name: String?,
            password_confirmation: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = repository.updateProfile(email, password_old, password, name, password_confirmation)))
        } catch (exception: HttpException) {
            emit(Resource.error(data = null, message = getErrorMsg(exception)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
}