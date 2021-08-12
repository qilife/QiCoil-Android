package com.Meditation.Sounds.frequencies.feature.playlist.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.Meditation.Sounds.frequencies.models.Playlist
import com.Meditation.Sounds.frequencies.models.PlaylistItem
import com.Meditation.Sounds.frequencies.models.PlaylistItemSongAndSong
import io.reactivex.Single

class PlaylistDetailViewModel(application: Application) : AndroidViewModel(application) {
    private var mPlayListDetailRepository = PlaylistDetailRepository(application)


    fun getAllPlayList(): LiveData<List<Playlist>> {
        return mPlayListDetailRepository.getAllPlayLists()
    }


    fun getPlaylist(playlistId: Long): LiveData<Playlist?> {
        return mPlayListDetailRepository.getPlaylistById(playlistId)
    }

    fun updatePlaylist(playlist: Playlist) {
        mPlayListDetailRepository.updatePlaylist(playlist)
    }

    fun updateDurationOfPlaylist(playlist: Playlist, duration : Long, mediaType: Int) {
        mPlayListDetailRepository.updateDurationOfPlaylist(playlist, duration, mediaType)
    }

    fun updateDurationOfAllPlaylist(playlist: Playlist, duration : Long, mediaType: Int, fromUser: Int) {
        mPlayListDetailRepository.updateDurationOfAllPlaylist(playlist, duration, mediaType,fromUser)
    }

    fun insertPlaylist(playlist: Playlist): Playlist {
        return mPlayListDetailRepository.insertPlaylist(playlist)
    }

    fun savePlaylistTask(playlistId: Long, items: ArrayList<PlaylistItem>): Single<ArrayList<PlaylistItem>> {
        return mPlayListDetailRepository.savePlaylist(playlistId, items)
    }

    fun getPlaylistItems(playlistId:Long):Single<ArrayList<PlaylistItem>> {
        return mPlayListDetailRepository.getPlaylistItems(playlistId)
    }
    fun addSongToPlaylist(playlistId: Long, item: PlaylistItem): PlaylistItem{
        return mPlayListDetailRepository.addSongToPlaylist(playlistId, item)
    }
    fun deleteSongFromPlaylistItem(playlistId: Long, item: PlaylistItem, song: PlaylistItemSongAndSong){
        mPlayListDetailRepository.deleteSongFromPlaylistItem(playlistId, item, song)
    }
}