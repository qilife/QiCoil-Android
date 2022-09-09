package com.Meditation.Sounds.frequencies.feature.album.detail

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.Meditation.Sounds.frequencies.db.QFDatabase
import com.Meditation.Sounds.frequencies.db.dao.PlaylistDAO
import com.Meditation.Sounds.frequencies.db.dao.SongDAO
import com.Meditation.Sounds.frequencies.models.Playlist
import com.Meditation.Sounds.frequencies.models.Song
import io.reactivex.Single


class AlbumDetailRepository(application: Application) {
    private var mSongDAO: SongDAO
    private var mPlaylistDAO: PlaylistDAO

    private var mSongs: LiveData<List<Song>>? = null
    private var mLastPlaylist: Playlist? = null
    private var mLastPlaylistId = 0

    init {
        val db = QFDatabase.getDatabase(application)
        mSongDAO = db.songDAO()
        mPlaylistDAO = db.playlistDAO()
    }


    fun getSongs(albumId: Long): LiveData<List<Song>> {
        if (mSongs == null) {
            mSongs = mSongDAO.getByAlbumIdAsLiveData(albumId)
        }
        return mSongs!!
    }

    fun getPlaylistById(playlistId: Long): Playlist? {
        return mPlaylistDAO.getById(playlistId)
    }

    fun getLastModifiedPlaylist(): Playlist? {
        return mPlaylistDAO.getLastModifiedPlaylist()
    }

    fun getLastModifiedPlaylistAsSingle(): Single<Playlist> {
        return Single.create { emitter ->
            val playlist = mPlaylistDAO.getLastModifiedPlaylist()
            if (playlist == null) {
                emitter.onSuccess(Playlist())
            } else {
                emitter.onSuccess(playlist)
            }
        }
    }

    fun getFirstModifiedPlaylistAsSingle(): Single<Playlist> {
        return Single.create { emitter ->
            val playlist = mPlaylistDAO.getFirstModifiedPlaylist()
            if (playlist == null) {
                emitter.onSuccess(Playlist())
            } else {
                emitter.onSuccess(playlist)
            }
        }
    }

    fun insert(song: Song) {
        InsertAsyncTask(mSongDAO).execute(song)
    }

    private class InsertAsyncTask internal constructor(private val mAsyncTaskDao: SongDAO) : AsyncTask<Song, Void, Void>() {

        override fun doInBackground(vararg params: Song): Void? {
            mAsyncTaskDao.insert(params[0])
            return null
        }
    }

    fun updateDurationSong(song: Song, duration: Long) {
        mSongDAO.updateDurationSong(song.id, duration)
    }

    fun updateFavoriteSong(song: Song, isAdd: Int) {
        mSongDAO.updateFavorite(song.id, song.albumId, isAdd)
    }
}
