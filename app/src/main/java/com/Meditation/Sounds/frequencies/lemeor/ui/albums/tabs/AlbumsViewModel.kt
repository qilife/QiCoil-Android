package com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.Meditation.Sounds.frequencies.lemeor.data.model.*

class AlbumsViewModel(private val repository: AlbumsRepository) : ViewModel() {

    val tiers = repository.getTiers()

    fun categoriesByTierId(id: Int): LiveData<List<Category>>? {
        return repository.getCategoriesByTierId(id)
    }

    fun albums(id: Int): LiveData<List<Album>>? {
        return repository.getAlbumsByCategory(id)
    }
}