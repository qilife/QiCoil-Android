package com.Meditation.Sounds.frequencies.lemeor.ui.rife

import androidx.lifecycle.ViewModel

class SearchRifeViewModel(private val repository: RifeRepository) : ViewModel() {

    fun getRifeList() = repository.getLiveDataRifes()
}