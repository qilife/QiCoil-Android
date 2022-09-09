package com.Meditation.Sounds.frequencies.feature.playlist.detail

import android.app.Application
import androidx.lifecycle.LiveData
import android.os.AsyncTask
import com.Meditation.Sounds.frequencies.db.QFDatabase
import com.Meditation.Sounds.frequencies.db.dao.PlaylistDAO
import com.Meditation.Sounds.frequencies.db.dao.PlaylistItemDAO
import com.Meditation.Sounds.frequencies.db.dao.PlaylistItemSongDAO
import com.Meditation.Sounds.frequencies.models.Playlist
import com.Meditation.Sounds.frequencies.models.PlaylistItem
import com.Meditation.Sounds.frequencies.models.PlaylistItemSongAndSong
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import io.reactivex.Single
import java.util.*
import kotlin.collections.ArrayList


class PlaylistDetailRepository(application: Application) {
    private var mPlaylistDAO: PlaylistDAO
    private var mPlaylistItemDAO: PlaylistItemDAO
    private var mPlaylistItemSongDAO: PlaylistItemSongDAO

    private var mPlayLists: LiveData<List<Playlist>>

    init {
        val db = QFDatabase.getDatabase(application)
        mPlaylistDAO = db.playlistDAO()
        mPlaylistItemDAO = db.playlistItemDAO()
        mPlaylistItemSongDAO = db.playlistItemSongDAO()
        mPlayLists = mPlaylistDAO.getAllAsLiveData()
    }


    fun getAllPlayLists(): LiveData<List<Playlist>> {
        return mPlayLists
    }

    fun getPlaylistById(playlistId: Long): LiveData<Playlist?> {
        return mPlaylistDAO.getByIdAsLiveData(playlistId)
    }

    fun updatePlaylist(playlist: Playlist) {
        UpdateTask(mPlaylistDAO).execute(playlist)
    }

    fun updateDurationOfPlaylist(playlist: Playlist, duration: Long, mediaType: Int) {
        mPlaylistDAO.updateTotalDuartion(playlist.id, duration, mediaType)
    }

    fun updateDurationOfAllPlaylist(playlist: Playlist, duration: Long, mediaType: Int, fromUser: Int) {
        mPlaylistDAO.updateTotalDurationPlaylist(playlist.id, duration, mediaType,fromUser)
    }

    fun insertPlaylist(playlist: Playlist): Playlist {
        playlist.id = mPlaylistDAO.insert(playlist)
        return playlist
    }

    fun getPlaylistItems(playlistId: Long): Single<ArrayList<PlaylistItem>> {
        return Single.create { emitter ->
            val items = mPlaylistItemDAO.getByPlayList(playlistId)
            for (item in items) {
                item.songs.addAll(mPlaylistItemSongDAO.getByPlaylistItemId(item.id))
            }
            emitter.onSuccess(ArrayList(items))
        }
    }

    fun savePlaylist(playlistId: Long, items: List<PlaylistItem>): Single<ArrayList<PlaylistItem>> {
        return Single.create { emitter ->
            mPlaylistItemDAO.deleteByPlaylistId(playlistId)
            for (item in items) {
                if (item.id != 0L) {
                    item.id = 0
                }
                item.playlistId = playlistId
                item.id = mPlaylistItemDAO.insert(item)
                for (song in item.songs) {
                    song.item.id = 0
                    song.item.playlistItemId = item.id
                    song.item.id = mPlaylistItemSongDAO.insert(song.item)
                }
            }
            emitter.onSuccess(ArrayList(items))

        }

    }

    fun addSongToPlaylist(playlistId: Long, item: PlaylistItem): PlaylistItem {

        item.playlistId = playlistId
        item.id = mPlaylistItemDAO.insert(item)
        for (song in item.songs) {
            song.item.id = 0
            song.item.playlistItemId = item.id
            song.item.id = mPlaylistItemSongDAO.insert(song.item)
        }
        return item

    }

    fun deleteSongFromPlaylistItem(playlistId: Long, item: PlaylistItem, song: PlaylistItemSongAndSong) {
        if (item.id > 0) {
            if (song.item.id > 0)
                mPlaylistItemSongDAO.delete(song.item.id)
            if (item.songs.size == 0 || (item.songs.contains(song) && item.songs.size == 1)) {
                mPlaylistItemDAO.delete(item.id)
            }
        }

    }

    private class UpdateTask internal constructor(private val mAsyncTaskDao: PlaylistDAO) : AsyncTask<Playlist, Void, Void>() {

        override fun doInBackground(vararg params: Playlist): Void? {
            params[0].dateModified = Date().time

            mAsyncTaskDao.update(params[0])

            SharedPreferenceHelper.getInstance().lastOpenedPlaylistId = params[0].id
            return null
        }
    }

}
