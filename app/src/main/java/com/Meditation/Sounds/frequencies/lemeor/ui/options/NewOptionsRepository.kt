package com.Meditation.Sounds.frequencies.lemeor.ui.options

import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper

class NewOptionsRepository(private val apiHelper: ApiHelper, private val localData: DataBase) {
    suspend fun logout() = apiHelper.logout()
    suspend fun deleteuser(password: String) = apiHelper.deleteuser(password)
}