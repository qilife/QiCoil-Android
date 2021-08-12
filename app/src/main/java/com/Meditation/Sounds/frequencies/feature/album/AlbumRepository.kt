package com.Meditation.Sounds.frequencies.feature.album

import android.app.Application
import androidx.lifecycle.LiveData
import android.os.AsyncTask
import com.Meditation.Sounds.frequencies.db.QFDatabase
import com.Meditation.Sounds.frequencies.db.dao.AlbumDAO
import com.Meditation.Sounds.frequencies.models.Album

class AlbumRepository(application: Application) {
    private var mAlbumDAO: AlbumDAO

    private var mAlbums: LiveData<List<Album>>
    private var mAlbumAdvanced: LiveData<List<Album>>
    private var mAlbumHigherAbundance: LiveData<List<Album>>
    private var mAlbumHigherQuantum: LiveData<List<Album>>

    init {
        val db = QFDatabase.getDatabase(application)
        mAlbumDAO = db.albumDAO()
        mAlbums = mAlbumDAO.getAllAsLiveData()
        mAlbumAdvanced = mAlbumDAO.getAlbumAdvancedAsLiveData()
        mAlbumHigherAbundance = mAlbumDAO.getAlbumsHigherAbundance()
        mAlbumHigherQuantum = mAlbumDAO.getAlbumsHigherQuantum()
    }

    fun getAllAlbums(): LiveData<List<Album>> {
        return mAlbums
    }

    fun getAlbumAdvanced(): LiveData<List<Album>> {
        return mAlbumAdvanced
    }

    fun getAlbumsHigherAbundance(): LiveData<List<Album>> {
        return mAlbumHigherAbundance
    }

    fun getAlbumsHigherQuantum(): LiveData<List<Album>> {
        return mAlbumHigherQuantum
    }

    fun insert(album: Album) {
        InsertAsyncTask(mAlbumDAO).execute(album)
    }

    private class InsertAsyncTask internal constructor(private val mAsyncTaskDao: AlbumDAO) : AsyncTask<Album, Void, Void>() {

        override fun doInBackground(vararg params: Album): Void? {
            mAsyncTaskDao.insert(params[0])
            return null
        }
    }
}
