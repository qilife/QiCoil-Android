package com.Meditation.Sounds.frequencies.tasks

import android.app.Application
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.db.QFDatabase
import com.Meditation.Sounds.frequencies.feature.playlist.PlaylistRepository
import com.Meditation.Sounds.frequencies.feature.playlist.detail.PlaylistDetailRepository

class UpdatePlaylistInforVer10Task (var application: Application, listener: ApiListener<Any>) : BaseTask<Any>(application, listener) {
    private var mPlayListRepository = PlaylistRepository(application)
    private var mPlayListDetailRepository = PlaylistDetailRepository(application)

    @Throws(Exception::class)
    override fun callApiMethod(): Any? {
        var playlists = mPlayListRepository.getPlaylists().blockingGet()
        for (item in playlists){
            var playlistItems = mPlayListDetailRepository.getPlaylistItems(item.id).blockingGet()
            for (it in playlistItems){
                for (song in it.songs){
                    QFDatabase.getDatabase(application).playlistItemSongDAO().updateStartEndOffset(song.item.id, 0, song.song.duration)
                }
            }
        }
        return null
    }

}