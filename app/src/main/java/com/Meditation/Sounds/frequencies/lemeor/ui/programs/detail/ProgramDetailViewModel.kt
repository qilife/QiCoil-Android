package com.Meditation.Sounds.frequencies.lemeor.ui.programs.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.tools.player.MusicRepository
import com.Meditation.Sounds.frequencies.utils.doubleOrString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs


class ProgramDetailViewModel(private val repository: ProgramDetailRepository) : ViewModel() {
    fun program(id: Int): LiveData<Program> {
        return repository.getProgramById(id)
    }

    suspend fun getTrackById(id: Int): Track? {
        return repository.getTrackById(id)
    }

    suspend fun getAlbumById(id: Int, categoryId: Int): Album? {
        return repository.getAlbumById(id, categoryId)
    }


    fun convertData(program: Program, onResult: (List<Search>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val searchResults = program.records.mapIndexedNotNull { index, s ->
                when (val value = s.doubleOrString()) {
                    is Double -> createSearchFromDouble(value, index)
                    is String -> createSearchFromString(value, index)
                    else -> null
                }
            }
            withContext(Dispatchers.Main) {
                onResult.invoke(searchResults)
            }
        }
    }

    private suspend fun createSearchFromDouble(num: Double, index: Int): Search? {
        return if (num >= 0) {
            withContext(Dispatchers.IO) {
                val track = getTrackById(num.toInt())
                track?.let {
                    val album = getAlbumById(it.albumId, it.category_id)
                    it.album = album
                    Search(index, it)
                }
            }
        } else {
            Search(
                index, MusicRepository.Frequency(
                    index,
                    "",
                    (abs(num)).toFloat(),
                    -index,
                    index,
                    false,
                    0,
                    0,
                )
            )

        }

    }

    private fun createSearchFromString(s: String, index: Int): Search? {
        return try {
            val listNum = s.split("|")
            val id = listNum.first().toDouble()
            val num = listNum.last().toDouble()
            Search(
                index, MusicRepository.Frequency(
                    index,
                    "",
                    (abs(num)).toFloat(),
                    -index,
                    index,
                    false,
                    0,
                    0,
                )
            )
        } catch (_: Exception) {
            null
        }
    }
}