package com.Meditation.Sounds.frequencies.lemeor.ui.options.change_pass

import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper

class ChangePassRepository(private val apiHelper: ApiHelper) {

    suspend fun updateProfile(
            email: String,
            password_old: String,
            password: String,
            name: String?,
            password_confirmation: String
    ) = apiHelper.updateProfile(email, password_old, password, name, password_confirmation)
}