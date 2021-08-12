package com.Meditation.Sounds.frequencies.tasks

import android.content.Context
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.api.objects.ForgotPasswordInput

class ForgotPasswordTask(context: Context, private var mInput: ForgotPasswordInput, listener: ApiListener<Any>) : BaseTask<Any>(context, listener) {
    override fun callApiMethod(): Any {
        return mApi.forgotPassword(mInput)
    }
}
