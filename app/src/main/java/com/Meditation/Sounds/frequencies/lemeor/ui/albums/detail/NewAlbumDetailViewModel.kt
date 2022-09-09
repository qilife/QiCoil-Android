package com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album

class NewAlbumDetailViewModel(private val repository: AlbumDetailRepository) : ViewModel() {
    fun album(id: Int): LiveData<Album>? {
        return repository.getAlbumsById(id)
    }
}