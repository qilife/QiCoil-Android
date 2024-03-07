package com.Meditation.Sounds.frequencies.lemeor.ui.programs

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Meditation.Sounds.frequencies.lemeor.FAVORITES
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.ui.main.UpdateTrack
import com.Meditation.Sounds.frequencies.utils.CombinedLiveData
import com.Meditation.Sounds.frequencies.utils.forEachBreak
import com.Meditation.Sounds.frequencies.utils.isNotString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewProgramViewModel(private val repository: ProgramRepository) : ViewModel() {

    suspend fun insert(program: Program?) {
        repository.insert(program)
    }

    suspend fun delete(program: Program?) {
        repository.delete(program)
    }

    suspend fun getTrackById(id: Int): Track? {
        return repository.getTrackById(id)
    }

    suspend fun getAlbumById(id: Int, categoryId: Int): Album? {
        return repository.getAlbumById(id, categoryId)
    }

    suspend fun createProgram(name: String) = repository.createProgram(name)
    suspend fun deleteProgram(idProgram: String) = repository.deleteProgram(idProgram)

    suspend fun updateTrackToProgram(track: UpdateTrack) = repository.updateTrackToProgram(track)

    suspend fun udpate(program: Program) {
        repository.update(program)
    }


    fun addTrackToProgram(id: Int, list: List<Search>, onDone: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val program = repository.getProgramById(id)
            val listT = arrayListOf<String>()
            val listR = arrayListOf<String>()
            program?.let { p ->
                list.forEach { s ->
                    if (s.obj is Track) {
                        val a = s.obj as Track
                        a.albumId
                        p.records.add(a.id.toString())
                        listT.add(a.id.toString())
                    } else if (s.obj is Rife) {
                        val r = s.obj as Rife
                        r.getFrequency().forEach { fre ->
                            val fr = "${r.id}|${fre}"
                            p.records.add(fr)
                            listR.add(fr)
                        }
                    }
                }

                repository.updateProgram(p)
                if (p.user_id.isNotEmpty()) {
                    try {
                        updateTrackToProgram(
                            UpdateTrack(
                                track_id = listT,
                                id = p.id,
                                "mp3",
                                request_type = "add",
                                is_favorite = (p.name.uppercase() == FAVORITES.uppercase() && p.favorited)
                            )
                        )
                        updateTrackToProgram(
                            UpdateTrack(
                                track_id = listR,
                                id = p.id,
                                "rife",
                                request_type = "add",
                                is_favorite = (p.name.uppercase() == FAVORITES.uppercase() && p.favorited)
                            )
                        )
                        withContext(Dispatchers.Main) {
                            onDone?.invoke()
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    fun addFrequencyToProgram(id: Int, frequency: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val program = repository.getProgramById(id)
            program?.let { p ->
                p.records.add("${-frequency}")
                repository.updateProgram(p)
                if (p.user_id.isNotEmpty()) {
                    try {
                        updateTrackToProgram(
                            UpdateTrack(
                                track_id = listOf("${-frequency}"),
                                id = p.id,
                                "rife",
                                request_type = "add",
                                is_favorite = (p.name.uppercase() == FAVORITES.uppercase() && p.favorited)
                            )
                        )
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    fun getPrograms(owner: LifecycleOwner, onChange: (List<Program>) -> Unit) {
        CombinedLiveData(repository.getListProgram(),
            repository.getListTrack(),
            combine = { listA, listT ->
                return@CombinedLiveData listA?.isNotEmpty() ?: false && listT?.isNotEmpty() ?: false
            }).observe(owner) { isCompletedData ->
            if (isCompletedData) {
                repository.getListProgram().observe(owner) { list ->
                    viewModelScope.launch(Dispatchers.IO) {
                        val programs = async { checkUnlocked(list) }
                        withContext(Dispatchers.Main) {
                            onChange.invoke(programs.await())
                        }
                    }
                }
            }
        }
    }

    private suspend fun checkUnlocked(list: List<Program>): List<Program> {
        list.forEach { program ->
            val tracks = program.records.filterNot { !it.isNotString() }
                .mapNotNull { it.toDoubleOrNull()?.toInt() }.mapNotNull { getTrackById(it) }
            val isUnlocked = tracks.forEachBreak { track ->
                val tempAlbum = getAlbumById(track.albumId, track.category_id)
                tempAlbum?.isUnlocked ?: true
            }
            program.isUnlocked = isUnlocked
        }
        return list
    }
}