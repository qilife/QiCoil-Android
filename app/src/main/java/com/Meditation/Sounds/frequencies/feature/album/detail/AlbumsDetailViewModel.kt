package com.Meditation.Sounds.frequencies.feature.album.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.Meditation.Sounds.frequencies.models.Playlist
import com.Meditation.Sounds.frequencies.models.Song
import io.reactivex.Single

class AlbumsDetailViewModel(application: Application) : AndroidViewModel(application) {
    private var mAlbumRepository = AlbumDetailRepository(application)


    fun getSongs(albumId: Long): LiveData<List<Song>> {
        return mAlbumRepository.getSongs(albumId)
    }
    fun getAlbumById(albumId: Long): LiveData<List<Song>> {
        return mAlbumRepository.getSongs(albumId)
    }

    fun getPlaylistById(playlistId: Long): Playlist? {
        return mAlbumRepository.getPlaylistById(playlistId)
    }
    fun getLastModifiedPlaylist(): Single<Playlist> {
        return mAlbumRepository.getLastModifiedPlaylistAsSingle()
    }

    fun getFirstModifiedPlaylist(): Single<Playlist> {
        return mAlbumRepository.getFirstModifiedPlaylistAsSingle()
    }

    fun getLastModified(playlistId: Long): Playlist? {
        return mAlbumRepository.getPlaylistById(playlistId)
    }

    fun updateDurationSong(song: Song, duration: Long) {
        mAlbumRepository.updateDurationSong(song, duration)
    }

    fun updateFavoriteSong(song: Song,isAdd: Int) {
        mAlbumRepository.updateFavoriteSong(song,isAdd)
    }
}
