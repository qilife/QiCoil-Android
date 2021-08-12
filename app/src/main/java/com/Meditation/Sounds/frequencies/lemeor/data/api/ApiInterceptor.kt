package com.Meditation.Sounds.frequencies.lemeor.data.api

import android.content.Context
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.preference
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.token

import okhttp3.Interceptor
import okhttp3.Response

class ApiInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val token: String? = preference(context).token

        request = request.newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer $token")
                .build()
        return chain.proceed(request)
    }
}