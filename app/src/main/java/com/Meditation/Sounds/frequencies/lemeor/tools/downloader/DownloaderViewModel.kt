package com.Meditation.Sounds.frequencies.lemeor.tools.downloader

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album

class DownloaderViewModel(private val repository: DownloaderRepository) : ViewModel() {
    fun album(id: Int, category_id: Int): LiveData<Album>? {
        return repository.getAlbumsById(id, category_id)
    }
}