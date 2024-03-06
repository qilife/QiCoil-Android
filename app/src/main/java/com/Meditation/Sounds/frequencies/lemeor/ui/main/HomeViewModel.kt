package com.Meditation.Sounds.frequencies.lemeor.ui.main

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.FAVORITES
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.HomeResponse
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.model.Search
import com.Meditation.Sounds.frequencies.lemeor.data.model.Status
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.utils.Resource
import com.Meditation.Sounds.frequencies.lemeor.data.utils.getErrorMsg
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class HomeViewModel(private val repository: HomeRepository, private val db: DataBase) :
    ViewModel() {
    //val home = repository.getHome(user_id)

    private var _pairData = MutableLiveData<List<Triple<String, List<Search>, Boolean>>>()
    val pairData: LiveData<List<Triple<String, List<Search>, Boolean>>> get() = _pairData

    private var _searchState = MutableLiveData<List<Triple<String, List<Search>, Boolean>>>()
    val searchState: LiveData<List<Triple<String, List<Search>, Boolean>>> get() = _searchState

    fun getHome(id: String): LiveData<Resource<HomeResponse>> {
        return repository.getHome(id)
    }

    fun getRife(): LiveData<Resource<List<Rife>>> {
        return repository.getRife()
    }

    fun getListAlbum() = repository.getListAlbum()
    fun getListTrack() = repository.getListTrack()

    fun getListRife() = repository.getListRife()

    fun setSearchKeyword(
        key: String,
        idPager: Int,
        context: Context,
        onSearch: (List<Triple<String, List<Search>, Boolean>>) -> Unit
    ) {
        try {
            if (idPager == 0) {
                _searchState.value?.let {
                    val s = it.first().second.filter { item ->
                        return@filter if (item.obj is Track) {
                            val track = item.obj as Track
                            track.name.lowercase().contains(key.lowercase())
                        } else false
                    }
                    val list = arrayListOf<Triple<String, List<Search>, Boolean>>()
                    list.add(
                        Triple(context.getString(R.string.tv_track), s, it.first().third)
                    )
                    list.add(
                        Triple(
                            context.getString(R.string.navigation_lbl_rife),
                            it.last().second,
                            it.last().third
                        )
                    )
                    onSearch.invoke(list)
                }
            } else if (idPager == 1) {
                _searchState.value?.let {
                    val s = it.last().second.filter { item ->
                        return@filter if (item.obj is Rife) {
                            val track = item.obj as Rife
                            track.title.lowercase().contains(key.lowercase())
                        } else false
                    }
                    val list = arrayListOf<Triple<String, List<Search>, Boolean>>()
                    list.add(
                        Triple(
                            context.getString(R.string.tv_track),
                            it.first().second,
                            it.first().third
                        )
                    )
                    list.add(
                        Triple(
                            context.getString(R.string.navigation_lbl_rife),
                            s,
                            it.last().third
                        )
                    )
                    onSearch.invoke(list)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun getLiveData(owner: LifecycleOwner, context: Context) {
        val list = arrayListOf<Triple<String, List<Search>, Boolean>>()
        list.add(Triple(context.getString(R.string.tv_track), arrayListOf(), true))
        list.add(Triple(context.getString(R.string.navigation_lbl_rife), arrayListOf(), true))
        var index = 0
        getListTrack().observe(owner) { listT ->
            CoroutineScope(Dispatchers.IO).launch {
                val listIT = listT.mapNotNull { parcelable ->
                    val track = parcelable as? Track
                    if (track != null) {
                        val album = getAlbumById(
                            track.albumId, track.category_id
                        )
                        Search(
                            ++index,
                            track.apply {
                                this.isUnlocked = album?.isUnlocked ?: false
                                this.album = album
                            })
                    } else {
                        null
                    }
                }
                list.firstOrNull()?.let { firstItem ->
                    list[0] = Triple(firstItem.first, listIT, listIT.isEmpty())
                }
                CoroutineScope(Dispatchers.Main).launch {
                    _pairData.value = list
                    _searchState.value = list
                }
            }
        }
        getListRife().observe(owner) { listR ->
            val listIR = listR.mapNotNull { parcelable ->
                val rife = parcelable as? Rife
                if (rife != null) {
                    Search(++index, rife)
                } else {
                    null
                }
            }
            list.lastOrNull()?.let { firstItem ->
                list[1] = Triple(firstItem.first, listIR, listIR.isEmpty())
            }
            _pairData.value = list
            _searchState.value = list
        }
    }

    fun getProfile() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = repository.getProfile()))
        } catch (exception: HttpException) {
            emit(Resource.error(data = null, message = getErrorMsg(exception)))
        } catch (exception: Throwable) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    suspend fun getApkList(): List<String> {
        return repository.getApkList()
    }

    suspend fun reportTrack(trackId: Int, trackUrl: String): Status {
        return repository.reportTrack(trackId, trackUrl)
    }

    suspend fun getAlbumById(id: Int, category_id: Int): Album? {
        return repository.getAlbumById(id, category_id)
    }

    suspend fun searchAlbum(searchString: String): List<Album> {
        return repository.searchAlbum(searchString)
    }

    suspend fun searchTrack(searchString: String): List<Track> {
        return repository.searchTrack(searchString)
    }

    suspend fun searchProgram(searchString: String): List<Program> {
        return repository.searchProgram(searchString)
    }

    fun loadFromCache(context: Context) {
        val cache: HomeResponse = Gson().fromJson(
            context.assets.open("db_caсhe.json").bufferedReader().use { reader ->
                reader.readText()
            }, HomeResponse::class.java
        )

        CoroutineScope(Dispatchers.IO).launch { repository.localSave(cache) }
    }

    fun loadDataLastHomeResponse(context: Context) {
        val homeResponse = PreferenceHelper.getLastHomeResponse(context)
        if (homeResponse?.tiers != null && homeResponse.tiers.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch { repository.localSave(homeResponse) }
        }
    }

    fun syncProgramsToServer() = viewModelScope.launch {
        try {
//            val localData = db.programDao().getData(true).toMutableList()
            val localAllData = db.programDao().getAllData().toMutableList()
            if (localAllData.isNotEmpty()) {
                val delete = localAllData.filter { it.deleted }
                if (delete.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        delete.forEach {
                            try {
                                repository.deleteProgram(it.id.toString())
                                db.programDao().delete(it)
                            } catch (_: Throwable) {
                            }
                        }
                    }
                }
                val syncData = localAllData.filter { !it.deleted }
                if (syncData.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        try {
                            val dataToSend = syncData.map {
                                Update(
                                    id = if (it.user_id.isEmpty()) -1 else it.id,
                                    name = it.name,
                                    favorited = (it.name.lowercase() == FAVORITES.lowercase() && it.favorited),
                                    tracks = it.records.toList()
                                )
                            }
                            repository.syncProgramsApi(dataToSend)
                        } catch (_: Throwable) {
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
    }
}

data class Update(
    val id: Int = 0,
    val name: String = "",
    val favorited: Boolean = false,
    var tracks: List<String> = listOf()
)

data class UpdateTrack(
    var track_id: List<String> = listOf(),
    var id: Int = 0,
    var track_type: String = "mp3",
    var request_type: String = "add",
    var is_favorite: Boolean = false,
)