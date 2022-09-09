package com.Meditation.Sounds.frequencies.feature.album

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.Meditation.Sounds.frequencies.models.Album

class AlbumsViewModel(application: Application) : AndroidViewModel(application){
    private var mAlbumRepository = AlbumRepository(application)

    fun getAllAlbumBasic():LiveData<List<Album>> {
        return mAlbumRepository.getAllAlbums()
    }

    fun getAlbumAdvanced() : LiveData<List<Album>> {
        return mAlbumRepository.getAlbumAdvanced()
    }

    fun getAlbumsHigherAbundance() : LiveData<List<Album>> {
        return mAlbumRepository.getAlbumsHigherAbundance()
    }

    fun getAlbumsHigherQuantum() : LiveData<List<Album>> {
        return mAlbumRepository.getAlbumsHigherQuantum()
    }

    fun insert(album: Album) {
        mAlbumRepository.insert(album)
    }
}