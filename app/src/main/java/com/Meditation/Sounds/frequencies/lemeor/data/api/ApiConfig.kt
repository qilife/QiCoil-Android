package com.Meditation.Sounds.frequencies.lemeor.data.api

import com.Meditation.Sounds.frequencies.BuildConfig

object ApiConfig {
    private const val https = "https://"
    private const val qi_coil_api = "www.combined.ingeniusstudios.com"
    private const val qi_coil_storage = "www.qicoilapi.ingeniusstudios.com"
    private const val quantum_api = "combined-quantum.ingeniusstudios.com"
    private const val quantum_storage = "quantumapi.ingeniusstudios.com"
    private const val api = "api/"
    private const val public = "/public/"
    private const val storage = "/storage/app/public/uploads"

    const val TIME_OUT = 200.toLong()
    
    const val API_HOME = "home"
    const val API_USER_LOGIN = "user/login"
    const val API_USER_ALBUM = "checkfreealbum"
    const val API_SAVE_ALBUM = "savefreealbum"
    const val API_USER_REGISTER = "user/register"
    const val API_USER_PROFILE = "user/profile"
    const val API_USER_PROFILE_UPDATE = "user/profile/update"
    const val API_USER_LOGOUT = "user/logout"
    const val API_USER_DELETE = "user/profile/delete"
    const val API_APK = "apk"
    const val API_SUBMIT_TRACK_ERROR = "reportTrack"
    const val API_SUBMIT_PROGRAM = "submitProgram"
    const val API_RIFE = "rifes"
    const val API_PROGRAMS_UPDATE = "programs/updateTrack"
    const val API_PROGRAMS = "programs"
    const val API_PROGRAMS_SYNC = "programs/sync"

    private const val API_RESET_PASS = "password/reset"

    fun getBaseUrl(): String {
        return if (BuildConfig.IS_FREE) {
            https + quantum_api + public + api
        } else {
            https + qi_coil_api + public + api
        }
    }

    fun getStorage(): String {
        return if (BuildConfig.IS_FREE) {
            https + quantum_storage + storage
        } else {
            https + qi_coil_storage + storage
        }
    }

    fun getPassResetUrl(): String {
        return if (BuildConfig.IS_FREE) {
            https + quantum_api + public + API_RESET_PASS
        } else {
            https + qi_coil_api + public + API_RESET_PASS
        }
    }
}