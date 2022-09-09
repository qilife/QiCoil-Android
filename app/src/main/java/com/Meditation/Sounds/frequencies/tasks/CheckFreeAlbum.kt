package com.Meditation.Sounds.frequencies.tasks

import android.content.Context
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.api.objects.CheckAlbumInput

class CheckFreeAlbum(context: Context, private var mInput: CheckAlbumInput, listener: ApiListener<Any>) : BaseTask<Any>(context, listener) {
    override fun callApiMethod(): Any {
        return mApi.checkAlbums(mInput)
    }
}