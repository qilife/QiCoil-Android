package com.Meditation.Sounds.frequencies.feature.main

import android.app.Application
import com.Meditation.Sounds.frequencies.db.QFDatabase
import com.Meditation.Sounds.frequencies.db.dao.AlbumDAO
import io.reactivex.Single


class MainActivityRepository(application: Application) {
    private var mAlbumDAO: AlbumDAO


    init {
        val db = QFDatabase.getDatabase(application)
        mAlbumDAO = db.albumDAO()
    }


    fun hasData(): Single<Boolean> {
        return Single.create { emitter ->
            val downloadedCount = mAlbumDAO.getDownloadedCount(true)
            val unDownloadCount = mAlbumDAO.getDownloadedCount(false)

            emitter.onSuccess(downloadedCount>0 && unDownloadCount==0)
        }
    }

}
