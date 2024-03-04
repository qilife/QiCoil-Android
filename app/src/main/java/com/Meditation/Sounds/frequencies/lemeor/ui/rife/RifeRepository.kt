package com.Meditation.Sounds.frequencies.lemeor.ui.rife

import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper

class RifeRepository(private val apiHelper: ApiHelper, private val localData: DataBase) {

    fun getLiveDataRifes() = localData.rifeDao().getLiveDataRifes()
    suspend fun getListRife() = localData.rifeDao().getData()
}