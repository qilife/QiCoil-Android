package com.Meditation.Sounds.frequencies.tasks

import android.content.Context
import com.Meditation.Sounds.frequencies.api.ApiListener

class GetProfileTask(context: Context, listener: ApiListener<Any>) : BaseTask<Any>(context, listener) {
    override fun callApiMethod(): Any {
        return mApi.profile
    }
}
