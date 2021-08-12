package com.Meditation.Sounds.frequencies.tasks

import android.content.Context
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.api.objects.ChangePasswordInput

class ChangePasswordTask(context: Context, private var mInput: ChangePasswordInput, listener: ApiListener<Any>) : BaseTask<Any>(context, listener) {
    override fun callApiMethod(): Any {
        return mApi.changePassword(mInput)
    }
}
