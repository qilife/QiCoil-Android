package com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import kotlinx.coroutines.launch

class NewAlbumDetailViewModel(private val repository: AlbumDetailRepository) : ViewModel() {
    fun album(id: Int, category_id: Int): LiveData<Album>? {
        return repository.getAlbumsById(id, category_id)
    }

    fun addRife(rife: Rife) = repository.insertRife(rife)

    fun getRife(rife: Rife) = repository.getRife(rife)
}