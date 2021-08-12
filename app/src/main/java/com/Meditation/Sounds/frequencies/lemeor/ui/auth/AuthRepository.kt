package com.Meditation.Sounds.frequencies.lemeor.ui.auth

import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper

class AuthRepository(private val apiHelper: ApiHelper, private val localData: DataBase) {

    suspend fun login(
            email: String,
            password: String
    ) = apiHelper.login(email, password)

    suspend fun register(
            email: String,
            password: String,
            password_confirmation: String,
            name: String,
            uuid: String
    ) = apiHelper.register(email, password, password_confirmation, name, uuid)
}