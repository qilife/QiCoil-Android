package com.Meditation.Sounds.frequencies.lemeor.data.remote

import com.Meditation.Sounds.frequencies.lemeor.data.api.ApiService

class ApiHelper(private val apiService: ApiService) : BaseDataSource() {
    suspend fun getHome() = getResult{ apiService.getHome() }

    suspend fun login(
            email: String,
            password: String
    ) = apiService.login(email, password)

    suspend fun register(
            email: String,
            password: String,
            password_confirmation: String,
            name: String,
            uuid: String
    ) = apiService.register(email, password, password_confirmation, name, uuid, 0)

    suspend fun googleLogin(
        email: String,
        gg_id: String,
        name: String,
    ) = apiService.googleLogin(email, gg_id, name, 0)

    suspend fun logout() = apiService.logout()

    suspend fun deleteuser(password: String) = apiService.deleteuser(password)

    suspend fun getApkList() = apiService.getApkList()

    suspend fun getProfile() = apiService.getProfile()

    suspend fun updateProfile(
            email: String,
            password_old: String,
            password: String,
            name: String?,
            password_confirmation: String
    ) = apiService.updateProfile(email, password_old, password, name, password_confirmation)
}