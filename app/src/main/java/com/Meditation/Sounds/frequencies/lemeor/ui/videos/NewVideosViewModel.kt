package com.Meditation.Sounds.frequencies.lemeor.ui.videos

import androidx.lifecycle.ViewModel

class NewVideosViewModel(repository: VideoRepository) : ViewModel() {
    val playlists = repository.getPlaylists()
}