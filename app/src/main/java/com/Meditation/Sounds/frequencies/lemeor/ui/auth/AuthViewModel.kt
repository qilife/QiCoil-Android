package com.Meditation.Sounds.frequencies.lemeor.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.Meditation.Sounds.frequencies.lemeor.data.utils.Resource
import com.Meditation.Sounds.frequencies.lemeor.data.utils.getErrorMsg
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException

class AuthViewModel (private val repository: AuthRepository) : ViewModel() {

    fun login(
            email: String,
            pass: String
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = repository.login(email, pass)))
        } catch (exception: HttpException) {
            emit(Resource.error(data = null, message = getErrorMsg(exception)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun register(
            email: String,
            password: String,
            password_confirmation: String,
            name: String,
            uuid: String
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = repository.register(email, password, password_confirmation, name, uuid)))
        } catch (exception: HttpException) {
            emit(Resource.error(data = null, message = getErrorMsg(exception)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
}