package com.Meditation.Sounds.frequencies.lemeor.ui.rife

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import kotlinx.coroutines.launch

class NewRifeViewModel(private val repository: RifeRepository) : ViewModel() {

    private var listRife = listOf<Rife>()

    private val _result = MutableLiveData<List<Rife>>()

    val result: LiveData<List<Rife>>
        get() = _result

    fun getRifeList() = repository.getAllRife()

   fun getRifeLocal(onDone:(List<Rife>)-> Unit){
       viewModelScope.launch {
           listRife = repository.getListRife()
           onDone.invoke(listRife)
       }
    }

    fun search(keySearch: String) {
        if (keySearch == "" || keySearch.isEmpty()) {
            _result.value = listRife.sortedWith(compareBy<Rife> {
                when {
                    it.title.lowercase().firstOrNull()?.isLetter() == true -> 0
                    else -> 1
                }
            }.thenBy { it.title.lowercase() })
        } else {
            _result.value = listRife.filter { it.title.lowercase().contains(keySearch.lowercase()) }
                .sortedBy { it.title.lowercase().indexOf(keySearch.lowercase()) != 0 }
        }
    }

    fun searchMain(keySearch: String) {
        if (keySearch == "" || keySearch.isEmpty()) {
            _result.value = arrayListOf()
        } else {
            _result.value = listRife.filter { it.title.lowercase().contains(keySearch.lowercase()) }
                .sortedBy { it.title.lowercase().indexOf(keySearch.lowercase()) != 0 }
        }
    }
}