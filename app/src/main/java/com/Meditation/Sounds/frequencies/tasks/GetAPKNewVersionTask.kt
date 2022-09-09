package com.Meditation.Sounds.frequencies.tasks

import android.content.Context
import com.Meditation.Sounds.frequencies.api.ApiListener

class GetAPKNewVersionTask(context: Context, listener: ApiListener<Any>) : BaseTask<Any>(context, listener) {

    @Throws(Exception::class)
    override fun callApiMethod(): Any {
        val tokenOutput = mApi.token
        mApi.setCredentials("Bearer ".plus(tokenOutput.token))
        return mApi.apKsNewVersion
    }
}
