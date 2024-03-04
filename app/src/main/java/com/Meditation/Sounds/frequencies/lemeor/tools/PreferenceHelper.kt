package com.Meditation.Sounds.frequencies.lemeor.tools

import android.content.Context
import android.content.SharedPreferences
import com.Meditation.Sounds.frequencies.BuildConfig
import com.Meditation.Sounds.frequencies.lemeor.data.model.HomeResponse
import com.Meditation.Sounds.frequencies.lemeor.data.model.User
import com.google.gson.Gson

object PreferenceHelper {

    private const val IF_FIRST_SYNC = "first_sync"
    private const val FLASH_SALE_REMAIN = "flash_sale_remain"
    private const val FLASH_SALE_TIME_STAMP = "flash_sale_time_stamp"
    private const val IF_FLASH_SALE_PURCHASED = "is_flash_sale_purchased"
    private const val IS_SHOW_DISCLAIMER = "is_show_disclaimer"
    private const val IS_LOGGED = "is_logged"
    private const val TOKEN = "token"
    private const val USER = "user"
    private const val LANGUAGE = "language"
    private const val HOME_RESPONSE = "home_response"

    private const val IS_HIGH_QUANTUM = "is_high_quantum"
    private const val IS_INNER_CIRCLE = "is_inner_circle"

    fun preference(context: Context): SharedPreferences = context.getSharedPreferences(BuildConfig.PREF_TITLE, Context.MODE_PRIVATE)

    private inline fun SharedPreferences.editMe(operation: (SharedPreferences.Editor) -> Unit) {
        val editMe = edit()
        operation(editMe)
        editMe.apply()
    }

    fun SharedPreferences.Editor.put(pair: Pair<String, Any>) {
        val key = pair.first
        when (val value = pair.second) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            else -> error("Only primitive types can be stored in SharedPreferences")
        }
    }

    fun getUser(context: Context): User? {
        return Gson().fromJson(preference(context).user, User::class.java)
    }

    fun saveUser(context: Context, user: User?) {
        preference(context).user = Gson().toJson(user)
    }

    var SharedPreferences.token
        get() = getString(TOKEN, "")
        set(value) {
            editMe {
                it.putString(TOKEN, value)
            }
        }
    var SharedPreferences.codeLanguage
        get() = getString(LANGUAGE, null) ?: "en"
        set(value) {
            editMe {
                it.putString(LANGUAGE, value)
            }
        }

    private var SharedPreferences.user
        get() = getString(USER, "")
        set(value) {
            editMe {
                it.putString(USER, value)
            }
        }

    var SharedPreferences.isInnerCircle
        get() = getBoolean(IS_INNER_CIRCLE, false)
        set(value) {
            editMe {
                it.putBoolean(IS_INNER_CIRCLE, value)
            }
        }

    var SharedPreferences.isHighQuantum
        get() = getBoolean(IS_HIGH_QUANTUM, false)
        set(value) {
            editMe {
                it.putBoolean(IS_HIGH_QUANTUM, value)
            }
        }

    var SharedPreferences.isLogged
        get() = getBoolean(IS_LOGGED, false)
        set(value) {
            editMe {
                it.putBoolean(IS_LOGGED, value)
            }
        }

    var SharedPreferences.isFirstSync
        get() = getBoolean(IF_FIRST_SYNC, true)
        set(value) {
            editMe {
                it.putBoolean(IF_FIRST_SYNC, value)
            }
        }

    var SharedPreferences.flashSaleRemain
        get() = getLong(FLASH_SALE_REMAIN, 86400000)
        set(value) {
            editMe {
                it.putLong(FLASH_SALE_REMAIN, value)
            }
        }

    var SharedPreferences.flashSaleTimeStamp
        get() = getLong(FLASH_SALE_TIME_STAMP, System.currentTimeMillis())
        set(value) {
            editMe {
                it.putLong(FLASH_SALE_TIME_STAMP, value)
            }
        }

    var SharedPreferences.isFlashSalePurchased
        get() = getBoolean(IF_FLASH_SALE_PURCHASED, false)
        set(value) {
            editMe {
                it.putBoolean(IF_FLASH_SALE_PURCHASED, value)
            }
        }

    var SharedPreferences.isShowDisclaimer
        get() = getBoolean(IS_SHOW_DISCLAIMER, true)
        set(value) {
            editMe {
                it.putBoolean(IS_SHOW_DISCLAIMER, value)
            }
        }

    fun getFlashSaleTime(context: Context): Long {
        if (preference(context).flashSaleTimeStamp == 86400000.toLong()) {
            return preference(context).flashSaleTimeStamp
        }
        return preference(context).flashSaleRemain - (System.currentTimeMillis() - preference(context).flashSaleTimeStamp)
    }


    fun getLastHomeResponse(context: Context): HomeResponse? {
        return Gson().fromJson(preference(context).homeResponse, HomeResponse::class.java)
    }

    fun saveLastHomeResponse(context: Context, homeResponse: HomeResponse?) {
        preference(context).homeResponse = Gson().toJson(homeResponse)
    }

    private var SharedPreferences.homeResponse
        get() = getString(HOME_RESPONSE, "")
        set(value) {
            editMe {
                it.putString(HOME_RESPONSE, value)
            }
        }
}