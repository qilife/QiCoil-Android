package com.Meditation.Sounds.frequencies.lemeor.data.api

import com.Meditation.Sounds.frequencies.lemeor.data.model.*
import com.Meditation.Sounds.frequencies.lemeor.ui.main.Update
import com.Meditation.Sounds.frequencies.lemeor.ui.main.UpdateTrack
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET(ApiConfig.API_HOME)
    suspend fun getHome(@Query("userid") userid: String): Response<HomeResponse>

    @GET(ApiConfig.API_USER_LOGIN)
    suspend fun login(
            @Query("email") email: String,
            @Query("password") password: String
    ): AuthResponse

    @GET(ApiConfig.API_USER_ALBUM)
    suspend fun checkAlbums(
            @Query("user_id") user_id: String
    ): Response<albumResponse>

    @GET(ApiConfig.API_SAVE_ALBUM)
    suspend fun SaveAlbums(
            @Query("user_id") user_id: String,
            @Query("album_id") album_id: String
    ): Response<albumResponse>

    @GET(ApiConfig.API_USER_REGISTER)
    suspend fun register(
            @Query("email") email: String,
            @Query("password") password: String,
            @Query("password_confirmation") password_confirmation: String,
            @Query("name") name: String,
            @Query("uuid") uuid: String,
            @Query("os") os: Int,
            @Query("flag") flag:Int
    ): AuthResponse

    @GET(ApiConfig.API_USER_LOGIN)
    suspend fun googleLogin(

        @Query("emailid") email: String,
        @Query("name") name: String,
        @Query("gg_id") gg_id: String,
        @Query("os") os: Int,
        @Query("flag") flag:Int
    ): AuthResponse

    @GET(ApiConfig.API_USER_LOGIN)
    suspend fun fbLogin(
        @Query("emailid") email: String,
        @Query("name") name: String,
        @Query("fb_id") fb_id: String,
        @Query("os") os: Int,
        @Query("flag") flag:Int
    ): AuthResponse


    @GET(ApiConfig.API_USER_PROFILE)
    suspend fun getProfile(): User

    @GET(ApiConfig.API_USER_PROFILE_UPDATE)
    suspend fun updateProfile(
            @Query("email") email: String,
            @Query("password_old") password_old: String,
            @Query("password") password: String,
            @Query("name") name: String?,
            @Query("password_confirmation") password_confirmation: String
    ): User

    @GET(ApiConfig.API_USER_LOGOUT)
    suspend fun logout(): Status

    @GET(ApiConfig.API_APK)
    suspend fun getApkList(): List<String>

    @GET(ApiConfig.API_USER_DELETE)
    suspend fun deleteuser(@Query("password") password: String): Status

    @GET(ApiConfig.API_SUBMIT_TRACK_ERROR)
    suspend fun reportTrack(
        @Query("track_id") track_id: Int,
        @Query("track_url") track_url: String
    ): Status

    @GET(ApiConfig.API_SUBMIT_PROGRAM)
    suspend fun submitProgram(
        @Query("track_id") track_id: Int,
        @Query("track_url") track_url: String
    ): Status

    @GET(ApiConfig.API_RIFE)
    suspend fun getRife(): Response<RifeResponse>


    @GET(ApiConfig.API_PROGRAMS)
    suspend fun getPrograms(): Response<ProgramsResponse>

    @POST(ApiConfig.API_PROGRAMS)
    suspend fun createPrograms(@Body map: Map<String, String>): ProgramCreateResponse

    @DELETE(ApiConfig.API_PROGRAMS + "/{program_id}")
    suspend fun deleteProgram(@Path("program_id") program_id: String): Status

    @POST(ApiConfig.API_PROGRAMS_SYNC)
    suspend fun syncProgramsToServer(@Body map: List<Update>): Status

    @POST(ApiConfig.API_PROGRAMS_UPDATE)
    suspend fun updateTrackToServer(@Body map: UpdateTrack): Status

}

