package com.Meditation.Sounds.frequencies.tasks

import android.app.Application
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.feature.playlist.PlaylistRepository
import com.Meditation.Sounds.frequencies.feature.playlist.detail.PlaylistDetailRepository
import com.Meditation.Sounds.frequencies.models.PlaylistArraysItem
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL

class UpdateDurationOfAllPlaylistTask(var application: Application, listener: ApiListener<Any>?) : BaseTask<Any>(application, listener) {
    private var mPlayListRepository = PlaylistRepository(application)
    private var mPlayListDetailRepository = PlaylistDetailRepository(application)
    private var isPurchasedBasic = SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)
    private var isPurchasedAdvanced = SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_ADVANCED)
    private var isPurchasedHigherAbundance = SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE)
    private var isPurchasedHigherQuantum = SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_HIGH_QUANTUM)

    @Throws(Exception::class)
    override fun callApiMethod(): Any? {
        var playlistArrays = arrayListOf<PlaylistArraysItem>()
        var playlistFromServerPath = SharedPreferenceHelper.getInstance().get(Constants.PREF_DEFAUT_PLAYLIST_JSON)
        if (playlistFromServerPath != null && playlistFromServerPath.length > 0) {
            var inputString: String? = null
            try {
                val url = URL(playlistFromServerPath)
                val buffer = BufferedReader(InputStreamReader(url.openStream()))
                inputString = buffer.use { it.readText() }
                buffer.close()
            } catch (e: MalformedURLException) {
            } catch (e: IOException) {
            }
            if (inputString != null) {
                SharedPreferenceHelper.getInstance().set(Constants.PREF_DEFAUT_PLAYLIST_CONTENT, inputString)
                playlistArrays = Gson().fromJson<java.util.ArrayList<PlaylistArraysItem>>(inputString, object : TypeToken<List<PlaylistArraysItem>>() {
                }.type)
            }
        }

        if (playlistArrays.size == 0) {
            var jsonFromLocal = SharedPreferenceHelper.getInstance().get(Constants.PREF_DEFAUT_PLAYLIST_CONTENT)
            if (jsonFromLocal != null && jsonFromLocal.length > 0) {
                playlistArrays = Gson().fromJson<java.util.ArrayList<PlaylistArraysItem>>(jsonFromLocal, object : TypeToken<List<PlaylistArraysItem>>() {
                }.type)
            }
        }

        var playlists = mPlayListRepository.getPlaylists().blockingGet()
        for (item in playlists) {
            var playlistItems = mPlayListDetailRepository.getPlaylistItems(item.id).blockingGet()
            var mediaType = Constants.MEDIA_TYPE_BASIC_FREE
            var totalTime = 0L
            var totalSongs = 0
            for (item in playlistItems) {
                var max = 0L
                for (s in item.songs) {
                    max = Math.max(max, s.item.endOffset - s.item.startOffset)
                    totalSongs++
                }
                totalTime += max
                for (it in item.songs) {
                    if (mediaType < it.song.mediaType!!) {
                        mediaType = it.song.mediaType!!
                    }
                }
            }
            var currentPlaylist: PlaylistArraysItem? = null
            if (item.fromUsers == 0) {//&& playlistItems.size == 0
                if (playlistArrays != null && playlistArrays.size > 0) {
                    for (pl in playlistArrays) {
                        if (pl.playlist_name.equals(item.title, ignoreCase = true)) {
                            currentPlaylist = pl
                            if (pl.songOfAlbum.size != playlistItems.size) {
                                if (mediaType < item.mediaType!!) {
                                    mediaType = item.mediaType!!
                                }
                            }
                            break
                        }
                    }
                }
            }
            if (currentPlaylist != null && item.fromUsers == 0) {
                if (!isUnClocked(item.mediaType!!) && currentPlaylist.songOfAlbum != null && totalSongs != currentPlaylist.songOfAlbum.size) {
                    totalTime = currentPlaylist.songOfAlbum.size.toLong() * 20 * 60 * 1000
                }
            }
            mPlayListDetailRepository.updateDurationOfPlaylist(item, totalTime, mediaType)
        }
        return null
    }

    fun isUnClocked(mediaType: Int): Boolean {
        return when {
            mediaType == Constants.MEDIA_TYPE_ADVANCED -> isPurchasedAdvanced //check advanced playlist
            mediaType < Constants.MEDIA_TYPE_ADVANCED -> {
                //check basic playlist
                if (isPurchasedBasic) {
                    mediaType == Constants.MEDIA_TYPE_BASIC || mediaType == Constants.MEDIA_TYPE_BASIC_FREE
                } else {
                    mediaType == Constants.MEDIA_TYPE_BASIC_FREE
                }
            }
            mediaType == Constants.MEDIA_TYPE_ABUNDANCE -> isPurchasedHigherAbundance
            mediaType == Constants.MEDIA_TYPE_HIGHER_QUANTUM -> isPurchasedHigherQuantum
            else -> true
        }
    }
}
