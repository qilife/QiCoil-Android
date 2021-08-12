package com.Meditation.Sounds.frequencies.feature.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.Single

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private var mRespository = MainActivityRepository(application)


    fun hasData():Single<Boolean> {
        return mRespository.hasData()
    }
}