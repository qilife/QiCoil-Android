package com.Meditation.Sounds.frequencies.tasks

import android.content.Context
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.api.objects.LoginInput

class LoginTask(context: Context, private var mInput: LoginInput, listener: ApiListener<Any>) : BaseTask<Any>(context, listener) {
    override fun callApiMethod(): Any {
        return mApi.loginByEmail(this.mInput)
    }
}
