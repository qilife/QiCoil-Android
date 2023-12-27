package com.Meditation.Sounds.frequencies.lemeor.data.api

import android.content.Context
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.preference
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.token

import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException
import java.net.UnknownHostException

class ApiInterceptor(private val context: Context) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            var request = chain.request()
            val token: String? = preference(context).token
            request = if (!request.headers.any { it.first == "Authorization" }) {
                request.newBuilder().addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer $token").build()
            } else {
                request.newBuilder().addHeader("Accept", "application/json").build()
            }
            return chain.proceed(request)
        } catch (e: Throwable) {
            if (e is UnknownHostException) {
                throw UnknownHostException("No address associated with hostname")
            } else throw e
        }
    }
}