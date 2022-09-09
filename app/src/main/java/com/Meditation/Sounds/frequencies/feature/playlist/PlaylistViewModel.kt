package com.Meditation.Sounds.frequencies.feature.playlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.Meditation.Sounds.frequencies.models.Playlist
import io.reactivex.Single

class PlaylistViewModel(application: Application):AndroidViewModel(application){
    private var mPlayListRepository = PlaylistRepository(application)


    fun getAllPlayList():LiveData<List<Playlist>>{
        return mPlayListRepository.getAllPlayLists()
    }

    fun insert(playlist: Playlist){
        mPlayListRepository.insert(playlist)
    }

    fun delete(playlistId: Long){
        mPlayListRepository.deletePlaylist(playlistId)
    }

    fun getPlaylists():Single<ArrayList<Playlist>> {
        return mPlayListRepository.getPlaylists()
    }
}