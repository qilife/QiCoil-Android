package com.Meditation.Sounds.frequencies.lemeor.data.api

import com.Meditation.Sounds.frequencies.lemeor.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET(ApiConfig.API_HOME)
    suspend fun getHome(): Response<HomeResponse>

    @GET(ApiConfig.API_USER_LOGIN)
    suspend fun login(
            @Query("email") email: String,
            @Query("password") password: String
    ): AuthResponse

    @GET(ApiConfig.API_USER_REGISTER)
    suspend fun register(
            @Query("email") email: String,
            @Query("password") password: String,
            @Query("password_confirmation") password_confirmation: String,
            @Query("name") name: String,
            @Query("uuid") uuid: String,
            @Query("os") os: Int
    ): AuthResponse

    @GET(ApiConfig.API_USER_LOGIN)
    suspend fun googleLogin(
        @Query("gg_id") password: String,
        @Query("emailid") email: String,
        @Query("name") name: String,
        @Query("os") os: Int
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
}

