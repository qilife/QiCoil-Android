package com.Meditation.Sounds.frequencies.tasks

import android.content.Context
import com.Meditation.Sounds.frequencies.api.ApiListener


/**
 * Created by dcmen on 13-Apr-17.
 */
class GetDefaultPlaylistTask(context: Context, listener: ApiListener<Any>) : BaseTask<Any>(context, listener) {

    @Throws(Exception::class)
    override fun callApiMethod(): Any {
        //Get new data
        val tokenOutput = mApi.token
        mApi.setCredentials("Bearer ".plus(tokenOutput.token))
        return mApi.defaultPlaylist

    }

}
