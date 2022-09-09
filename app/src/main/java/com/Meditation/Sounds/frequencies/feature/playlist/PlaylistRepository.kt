package com.Meditation.Sounds.frequencies.feature.playlist

import android.app.Application
import androidx.lifecycle.LiveData
import android.os.AsyncTask
import com.Meditation.Sounds.frequencies.db.QFDatabase
import com.Meditation.Sounds.frequencies.db.dao.PlaylistDAO
import com.Meditation.Sounds.frequencies.db.dao.PlaylistItemDAO
import com.Meditation.Sounds.frequencies.db.dao.PlaylistItemSongDAO
import com.Meditation.Sounds.frequencies.models.Playlist
import io.reactivex.Single

class PlaylistRepository(application: Application){
    private var mPlaylistDAO: PlaylistDAO
    private var mPlaylistItemDAO: PlaylistItemDAO
    private var mPlaylistItemSongDAO: PlaylistItemSongDAO
    private var mPlayLists:LiveData<List<Playlist>>

    init {
        val db = QFDatabase.getDatabase(application)
        mPlaylistDAO = db.playlistDAO()
        mPlayLists = mPlaylistDAO.getAllAsLiveData()
        mPlaylistItemDAO = db.playlistItemDAO()
        mPlaylistItemSongDAO = db.playlistItemSongDAO()
    }


    fun getAllPlayLists(): LiveData<List<Playlist>> {
        return mPlayLists
    }

    fun getPlaylists(): Single<ArrayList<Playlist>> {
        return Single.create { emitter ->
            val items = mPlaylistDAO.getAll()
            emitter.onSuccess(ArrayList(items))
        }
    }

    fun deletePlaylist(playlistId : Long){
        val items = mPlaylistItemDAO.getByPlayList(playlistId)
        for (item in items){
            mPlaylistItemSongDAO.deleteByPlaylistItemId(item.id)
        }
        mPlaylistDAO.delete(playlistId)
    }

    fun insert(playlist: Playlist) {
        InsertAsyncTask(mPlaylistDAO).execute(playlist)
    }

    private class InsertAsyncTask internal constructor(private val mAsyncTaskDao: PlaylistDAO) : AsyncTask<Playlist, Void, Void>() {

        override fun doInBackground(vararg params: Playlist): Void? {
            params[0].id = mAsyncTaskDao.insert(params[0])
            return null
        }
    }
}
