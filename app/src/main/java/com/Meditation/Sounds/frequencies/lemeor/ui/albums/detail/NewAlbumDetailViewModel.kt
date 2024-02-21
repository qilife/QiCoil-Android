package com.Meditation.Sounds.frequencies.lemeor.ui.albums.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewAlbumDetailViewModel(private val repository: AlbumDetailRepository) : ViewModel() {
    fun album(id: Int, category_id: Int): LiveData<Album>? {
        return repository.getAlbumsById(id, category_id)
    }

    fun addRife(rife: Rife) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.insertRife(rife)
        }
    }
}