package com.Meditation.Sounds.frequencies.lemeor.data.api

import android.content.Context
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.preference
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.token

import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException
import java.net.InetAddress
import java.net.UnknownHostException

class ApiInterceptor(private val context: Context) : Interceptor {
    @Suppress("UNREACHABLE_CODE")
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val token: String? = preference(context).token
        request = request.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()
        try {
            return chain.proceed(request)
        }catch (e: Throwable) {
            if (e is IOException) {
                throw e
            } else {
                throw IOException(e)
            }
        }
    }
}