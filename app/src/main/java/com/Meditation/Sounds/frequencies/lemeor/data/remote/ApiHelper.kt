package com.Meditation.Sounds.frequencies.lemeor.data.remote

import com.Meditation.Sounds.frequencies.lemeor.data.api.ApiService
import com.Meditation.Sounds.frequencies.lemeor.ui.main.Update
import com.Meditation.Sounds.frequencies.lemeor.ui.main.UpdateTrack

class ApiHelper(private val apiService: ApiService) : BaseDataSource() {
    suspend fun getHome(user_id : String) = getResult{ apiService.getHome(user_id) }

    suspend fun getCHeckHome(user_id : String) = getResult{ apiService.checkAlbums(user_id) }

    suspend fun SaveAlbum(user_id : String,album_id : String) = getResult{ apiService.SaveAlbums(user_id,album_id) }

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
    ) = apiService.register(email, password, password_confirmation, name, uuid, 0,1)

    suspend fun googleLogin(
        email: String,
        name: String,
        gg_id: String,
    ) = apiService.googleLogin(email, name, gg_id , 0,1)

    suspend fun fbLogin(
        email: String,
        name: String,
        fb_id: String
    ) = apiService.fbLogin(email, name,fb_id, 0,1)

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

    suspend fun reportTrack(trackId: Int, trackUrl: String) =
        apiService.reportTrack(trackId, trackUrl)

    suspend fun submitProgram(trackId: Int, trackUrl: String) =
        apiService.submitProgram(trackId, trackUrl)

    suspend fun getRife() = getResult { apiService.getRife() }


    suspend fun getPrograms() = getResult { apiService.getPrograms() }

    suspend fun createPrograms(name: String) = apiService.createPrograms(mapOf("name" to name))

    suspend fun deleteProgram(id: String) = apiService.deleteProgram(id)
    suspend fun updateTrackToProgram(track: UpdateTrack) = apiService.updateTrackToServer(track)

    suspend fun syncProgramsToServer(listProgram: List<Update>) =
        apiService.syncProgramsToServer(listProgram)
}