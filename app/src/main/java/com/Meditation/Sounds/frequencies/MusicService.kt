package com.Meditation.Sounds.frequencies

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import com.Meditation.Sounds.frequencies.feature.main.MainActivity
import com.Meditation.Sounds.frequencies.models.PlaylistItem
import com.Meditation.Sounds.frequencies.models.PlaylistItemSong
import com.Meditation.Sounds.frequencies.models.PlaylistItemSongAndSong
import com.Meditation.Sounds.frequencies.models.Song
import com.Meditation.Sounds.frequencies.utils.Constants
import java.util.*
import kotlin.collections.ArrayList


class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    enum class RepeatType {
        NONE, ONE, ALL
    }

    private val callbacks = ArrayList<Callback>()
    private val iSongPlaying = ArrayList<IGetSongPlaying>()

    //media player
    private var players = ArrayList<ExtendExoPlayer>()

    //song list
    private var playlistItems: ArrayList<PlaylistItem>? = null

    //current position
    private var songPosition: Int = 0

    //binder
    private val musicBind = MusicBinder()

    //title of current songonCompletion
    private var songTitle: String? = ""
    //shuffle flag and random

    var shuffle = false
    private var rand: Random? = null

    var repeatType = RepeatType.NONE

    private var isPaused = false
    private var numberOfTimeOnFail = 0

    var mDurationUpdateHandler: Handler = Handler()
    var longestDurationPlayer: ExtendExoPlayer? = null
    var isRequestAudioFocus = true


    var mCountDownTimerStart: CountDownTimer? = null
    var mCountDownTimerEnd: CountDownTimer? = null
    var mCountDownTimerStartFinish = false
    var mCountDownTimerEndFinish = false
    fun cancelCountDownStart() {
        if (mCountDownTimerStart != null) {
            mCountDownTimerStart!!.cancel()
        }
    }

    fun cancelCountDownEnd() {
        if (mCountDownTimerEnd != null) {
            mCountDownTimerEnd!!.cancel()
        }
    }

    private val mDurationUpdateRunnable = object : Runnable {
        override fun run() {
            try {
                if (longestDurationPlayer != null) {
                    mDurationUpdateHandler.postDelayed(this, 300)
                    if (longestDurationPlayer != null && longestDurationPlayer!!.isPrepared) {
                        val songFromPlayer = longestDurationPlayer!!.getSong()
                        if (songFromPlayer != null) {
                            try {

                                val currentPosition = (longestDurationPlayer!!.ratioDiv.toInt() * longestDurationPlayer!!.duration() + longestDurationPlayer!!.currentPosition()) - songFromPlayer.item.startOffset.toInt()
//                                var duration = longestDurationPlayer!!.duration - songFromPlayer.item.startOffset.toInt()
                                val totalDuration = songFromPlayer.item.endOffset.toInt() - songFromPlayer.item.startOffset.toInt()

//                                if (totalDuration - currentPosition <= 5000) {
//                                if (currentPosition<5000) {
//                                    setCountDownTimerStart(songFromPlayer)
//                                }
//
//                                val timerEnd = totalDuration - currentPosition
//                                if (timerEnd < 5000) {
//                                    setCountDownTimerEnd(songFromPlayer)
//                                }
//                                }

                                for (callback in callbacks) {
                                    callback.updateDurationPlayer(totalDuration, currentPosition, songFromPlayer)
                                }
                            } catch (ex: IllegalStateException) {
                            }
                        }
                    }
                }
            } catch (ex: IllegalStateException) {

            }
        }
    }

    //playback methods
    val currentPosition: Int
        get() {
            if (players.size > 0) {
                var currentPosition = 0
                for (player in players) {
                    currentPosition = Math.max(currentPosition, player.currentPosition())
                }
                return currentPosition
            }

            return 0
        }

    val duration: Int
        get() {
            if (players.size > 0) {
                var duration = 0
                for (player in players) {
                    duration = Math.max(duration, player.duration())
                }
                return duration
            }

            return 0
        }


    var isPlaying = false
    lateinit var mAudioManager: AudioManager

    var deltaValue: Float = 0f
    var speed: Float = 0.05f
    var timerList = ArrayList<Timer>()

    override fun onCreate() {
        //create the service
        super.onCreate()
        numberOfTimeOnFail = 0
        //initialize position
        songPosition = -1
        //random
        rand = Random()
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        //create player
//        player = MediaPlayer()
        //initialize
//        initMusicPlayer()
    }

    fun addCallback(callback: Callback) {
        callbacks.add(callback)
    }

    fun addCallbackSongPlaying(callback: IGetSongPlaying) {
        iSongPlaying.add(callback)
    }

    fun removeCallback(callback: Callback) {
        callbacks.remove(callback)
    }

    fun initMusicPlayer(): ExtendExoPlayer {
        val player = ExtendExoPlayer(this)
//        //set player properties
//        player.setWakeMode(applicationContext,
//                PowerManager.PARTIAL_WAKE_LOCK)
//        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        //set listeners
        player.setOnPreparedListener(this)
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)

        return player
    }

    //pass song list
    fun play(theSongs: ArrayList<PlaylistItem>) {
        playlistItems = ArrayList()
        //clone playlist
        for (item in theSongs) {
            val playlistItem = PlaylistItem()
            playlistItem.playlistId = item.playlistId
            playlistItem.id = item.id
            for (song in item.songs) {
                val itemSong = PlaylistItemSongAndSong()
                itemSong.song = song.song
                itemSong.item.startOffset = song.item.startOffset
                itemSong.item.endOffset = song.item.endOffset
                itemSong.item.volumeLevel = song.item.volumeLevel
                itemSong.item.id = song.item.id

                playlistItem.songs.add(itemSong)
            }
            playlistItems!!.add(playlistItem)
        }

        songPosition = -1
        playNext(false)
    }

    fun playItemSongPlaylist(item: PlaylistItem) {
        playlistItems = ArrayList()
        val playlistItem = PlaylistItem()
        playlistItem.playlistId = item.playlistId
        playlistItem.id = item.id
        for (song in item.songs) {
            val itemSong = PlaylistItemSongAndSong()
            itemSong.song = song.song
            itemSong.item.startOffset = song.item.startOffset
            itemSong.item.endOffset = song.item.endOffset
            itemSong.item.volumeLevel = song.item.volumeLevel
            itemSong.item.id = song.item.id
            playlistItem.songs.add(itemSong)
        }
        playlistItems!!.add(playlistItem)

        songPosition = -1
        playNext(false)
    }

    fun playSong(songs: ArrayList<Song>) {
        playlistItems = ArrayList()

        val songAndSongs = ArrayList<PlaylistItemSongAndSong>()
        for (song in songs) {
            val itemSongAndSong = PlaylistItemSongAndSong()
            val playlistItemSong = PlaylistItemSong()
            playlistItemSong.id = -1
            playlistItemSong.songId = song.id
            itemSongAndSong.item = playlistItemSong
            itemSongAndSong.song = song
            itemSongAndSong.item.startOffset = 0
            itemSongAndSong.item.endOffset = song.duration
            songAndSongs.add(itemSongAndSong)
        }

        for (i in 0..songAndSongs.size - 1) {
            val itemSong = PlaylistItemSongAndSong()
            val playlistItem = PlaylistItem()
            itemSong.song = songAndSongs[i].song
            itemSong.item.startOffset = 0
            itemSong.item.endOffset = songAndSongs[i].item.endOffset
            itemSong.item.volumeLevel = songAndSongs[i].item.volumeLevel
            itemSong.item.id = songAndSongs[i].item.id
            playlistItem.playlistId = -i.toLong()
            playlistItem.id = -i.toLong()
            if (playlistItem.songs.size == 0) {
                playlistItem.songs.add(itemSong)
            }

            playlistItems?.add(playlistItem)
        }

        songPosition = -1
        playNext(false)
    }

    fun playItemSongAlbum(song: Song) {
        playlistItems = ArrayList()

        val songAndSongs = ArrayList<PlaylistItemSongAndSong>()
        val itemSongAndSong = PlaylistItemSongAndSong()
        val playlistItemSong = PlaylistItemSong()
        playlistItemSong.id = -1
        playlistItemSong.songId = song.id
        itemSongAndSong.item = playlistItemSong
        itemSongAndSong.song = song
        itemSongAndSong.item.startOffset = 0
        itemSongAndSong.item.endOffset = song.duration
        songAndSongs.add(itemSongAndSong)

        for (i in 0..songAndSongs.size - 1) {
            val itemSong = PlaylistItemSongAndSong()
            val playlistItem = PlaylistItem()
            itemSong.song = songAndSongs[i].song
            itemSong.item.startOffset = 0
            itemSong.item.endOffset = songAndSongs[i].item.endOffset
            itemSong.item.volumeLevel = songAndSongs[i].item.volumeLevel
            itemSong.item.id = songAndSongs[i].item.id
            playlistItem.playlistId = -i.toLong()
            playlistItem.id = -i.toLong()
            if (playlistItem.songs.size == 0) {
                playlistItem.songs.add(itemSong)
            }

            playlistItems?.add(playlistItem)
        }

        songPosition = -1
        playNext(false)
    }

    //binder
    inner class MusicBinder : Binder() {
        internal val service: MusicService
            get() = this@MusicService
    }

    //activity will bind to service
    override fun onBind(intent: Intent): IBinder? {
        return musicBind
    }

    //release resources when unbind
    override fun onUnbind(intent: Intent): Boolean {
//        player!!.stop()
//        player!!.release()
        return false
    }

    //play a song
    fun playSong(isPauseImmediatelyStarted: Boolean) {
        if (playlistItems!!.size == 0) {
            return
        }
        isPaused = false
        for (player in players) {
            if (player.isPlaying())
                player.stop()
            player.release()
        }
        players.clear()
        longestDurationPlayer = null
        if (playlistItems != null && playlistItems!!.isNotEmpty()
                && songPosition > -1 && songPosition < playlistItems!!.size) {
            //reset all players
            val playItem = playlistItems!![songPosition]
            val playSongs = playItem.songs
            //set Volume
            if (playSongs.size > 0) {
                var max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round((playSongs.get(0).item.volumeLevel * max)), 0)
            }


            //remove unused players

            //create player for each song
            var longestDurationTmp: Long = 0
            for (i in 0 until playSongs.size) {
                val player = initMusicPlayer()
                player.isPauseImmediatelyStarted = isPauseImmediatelyStarted
                players.add(player)
                player.reset()

                if (isRequestAudioFocus) {
                    requestToSilentOtherMusicApps()
                }

                //get song
                val song = playSongs[i]
                player.play(song)


                //get song playing
                val name = song.song.title
                val songId = song.song.id
                val albumId = song.song.albumId
                for (callback in callbacks) {
                    callback.updateTitlePlayer(name, albumId)
                }
                for (callback in iSongPlaying) {
                    callback.songPlaying(songId)
                }

                //get longest duration song
//                if (longestDurationTmp <= (song.song.duration - song.item.startOffset)) {
//                    longestDurationTmp = song.song.duration - song.item.startOffset
//                    longestDurationPlayer = player
//                }
                if (longestDurationTmp <= (song.item.endOffset - song.item.startOffset)) {
                    longestDurationTmp = song.item.endOffset - song.item.startOffset
                    longestDurationPlayer = player
                }
            }

            //Send playlistitemn which is playing
            val intent = Intent(Constants.BROADCAST_PLAY_PLAYLIST)
            intent.putExtra(Constants.EXTRAX_PLAYLIST_ITEM_ID, playItem.id)
            intent.putExtra(Constants.EXTRAX_PLAYLIST_IS_PLAYING, true)
            sendBroadcast(intent)
        }

        for (callback in callbacks) {
            isPlaying = true
            callback.onPlaylistItemStart(playlistItems!![songPosition])
        }

        updateDurationPlayer()
    }

    fun requestToSilentOtherMusicApps() {
        isRequestAudioFocus = false
        mAudioManager.requestAudioFocus(object : AudioManager.OnAudioFocusChangeListener {
            override fun onAudioFocusChange(p0: Int) {
                when (p0) {
                    AudioManager.AUDIOFOCUS_GAIN, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        isRequestAudioFocus = false
                    } // Resume your media player here
                    AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        pausePlayer()
//                        mAudioManager.abandonAudioFocus(this)
                        isRequestAudioFocus = true
                    }// Pause your media player here
                }
            }

        }, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN)
    }

    fun updateDurationPlayer() {
        mDurationUpdateHandler.removeCallbacksAndMessages(null);
        if (longestDurationPlayer != null) {
            mDurationUpdateHandler.postDelayed(mDurationUpdateRunnable, 0)
        }
    }

    //set the song
    fun setSong(songIndex: Int) {
        songPosition = songIndex
    }

    override fun onCompletion(mp: MediaPlayer) {
        var isCompletedAll = true
        for (player in players) {
            if (!player.isCompleted) {
                isCompletedAll = false
                break
            }
        }
        if (isCompletedAll) {
            if (repeatType == RepeatType.ONE) {
                playSong(false)
            } else {
                if (repeatType == RepeatType.NONE && songPosition == playlistItems!!.size - 1) {
                    if (mDurationUpdateHandler != null) {
                        mDurationUpdateHandler.removeCallbacksAndMessages(null);
                    }
                    pausePlayer()
                    if (longestDurationPlayer != null) {
                        val songFromPlayer = longestDurationPlayer!!.getSong()
                        for (callback in callbacks) {
                            songFromPlayer?.let { callback.updateDurationPlayer(0, 0, it) }
                        }
                        longestDurationPlayer?.release()
                    }

                    playlistItems = null
                    //remove playing background
                    val intent = Intent(Constants.BROADCAST_PLAY_PLAYLIST)
                    intent.putExtra(Constants.EXTRAX_PLAYLIST_ITEM_ID, -1)
                    intent.putExtra(Constants.EXTRAX_PLAYLIST_IS_PLAYING, true)
                    intent.putExtra(Constants.EXTRAX_HIDDEN_CONTROLLER, true)
                    sendBroadcast(intent)
                    if (iSongPlaying != null) {
                        for (callback in iSongPlaying) {
                            callback.songPlaying(-1)
                        }
                    }
                } else {
                    playNext(true)
                }
            }
        }
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        Log.v("MUSIC PLAYER", "Playback Error")
//        mp?.reset()
        isPlaying = false
        for (callback in callbacks) {
            callback.onStopPlayback()
        }
        numberOfTimeOnFail++
        if (numberOfTimeOnFail >= 3) {
            numberOfTimeOnFail = 0
            sendBroadcast(Intent(Constants.ACTION_RE_DOWNLOAD_MP3))
        }
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        //start playback
//        startPlayback()
        numberOfTimeOnFail = 0
        var isReady = true
        for (player in players) {
            if (!player.isPrepared) {
                isReady = false
                break
            }
        }
        if (isReady && isPlaying) {
            startPlayback()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startPlayback() {
        var isPauseImmediatelyStarted = false
        if (players != null && players.size > 0) {
            isPauseImmediatelyStarted = players.get(0).isPauseImmediatelyStarted
        }
        for (player in players) {
            player.start()
            player.isPauseImmediatelyStarted = false
        }
        if (isPauseImmediatelyStarted) {
            pausePlayer()
        } else {
            for (callback in callbacks) {
                callback.onPlaylistItemPlayed(playlistItems!![songPosition])
            }
        }
        //notification
        val notIntent = Intent(this, MainActivity::class.java)
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        var builder = Notification.Builder(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appName = getString(R.string.app_name)
            val description = getString(R.string.app_name)
            val nm = getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager
            if (nm != null) {
                var chanelId = packageName + ".CHANNEL_ID_FOREGROUND"
                var nChannel: NotificationChannel? = nm.getNotificationChannel(chanelId)
                if (nChannel == null) {
                    nChannel = NotificationChannel(packageName + ".CHANNEL_ID_FOREGROUND", appName, NotificationManager.IMPORTANCE_MIN)
                    nChannel.description = description
                    nm.createNotificationChannel(nChannel)
                }
                builder = Notification.Builder(this, chanelId)
            }
        }

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle)
        val not = builder.notification
        startForeground(NOTIFY_ID, not)
    }

    fun pausePlayer() {
        if (playlistItems == null || (playlistItems != null && playlistItems!!.size == 0)) {
            return
        }
        isPlaying = false
        isPaused = true
        for (player in players) {
            player.pause()
        }
        for (callback in callbacks) {
            callback.onPausePlayback()
        }
        broadcastActionPlayler(false)
    }

    fun broadcastActionPlayler(isPlay: Boolean) {
        if (playlistItems != null && playlistItems!!.isNotEmpty()
                && songPosition > -1 && songPosition < playlistItems!!.size) {
            val playItem = playlistItems!![songPosition]
            val intent = Intent(Constants.BROADCAST_PLAY_PLAYLIST)
            intent.putExtra(Constants.EXTRAX_PLAYLIST_ITEM_ID, playItem.id)
            intent.putExtra(Constants.EXTRAX_PLAYLIST_IS_PLAYING, isPlay)
            sendBroadcast(intent)
        }
    }

    fun seek(position: Int) {
        if (playlistItems == null || (playlistItems != null && playlistItems!!.size == 0)) {
            return
        }
        for (player in players) {
            try {
                player.seekTo(position)
            } catch (e: IllegalStateException) {
                player.stop()
            }
        }
    }

    fun resume() {
        if (playlistItems != null) {
            isPlaying = true
            if (isPaused) {

                if (isRequestAudioFocus) {
                    requestToSilentOtherMusicApps()
                }

                isPaused = false
                for (player in players) {
                    player.startFromResume()
                    broadcastActionPlayler(true)
                }
            } else {
                if (songPosition >= playlistItems!!.size) songPosition = 0
                if (songPosition < playlistItems!!.size)
                    playSong(false)
            }
        }
    }

    fun changeDurationFromMain(progress: Int) {
        if (playlistItems == null || (playlistItems != null && playlistItems!!.size == 0)) {
            return
        }
        if (playlistItems != null) {
            for (player in players) {
                val song = player.getSong()
                if (song != null) {
                    if (song.item.startOffset + progress < song.item.endOffset) {
                        player.seekTo(song.item.startOffset.toInt() + progress)
                    }
                }
            }
        }
    }

    fun changeVolume(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
        if (playlistItems != null) {
            for (item in playlistItems!!) {
                if (playlistItem.id == item.id) {
                    for (songItem in item.songs) {
                        if (songItem.item.id == song.item.id) {
                            songItem.item.volumeLevel = song.item.volumeLevel
                            //reset volume in player
                            for (player in players) {
                                if (player.getSong()!!.item.id == song.item.id) {
//                                    player.setVolume(song.item.volumeLevel, song.item.volumeLevel)
                                    var max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round((song.item.volumeLevel * max)), 0)
                                    break
                                }
                            }
                            break
                        }
                    }
                    break
                }
            }
        }
    }

    fun changeDuration(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
        if (playlistItems != null) {
            for (item in playlistItems!!) {
                if (playlistItem.id == item.id) {
                    for (songItem in item.songs) {
                        if (songItem.item.id == song.item.id) {
//                            songItem.item.startOffset = song.item.startOffset
                            songItem.item.endOffset = song.item.endOffset
                            //restart player
                            for (player in players) {
                                if (player.getSong()!!.item.id == song.item.id) {
//                                    playSong(isPaused)
                                    player.resetDuration(song.item.endOffset)
                                    break
                                }
                            }
                            break
                        }
                    }
                    break
                }
            }
        }
    }

    fun deleteSong(playlistItem: PlaylistItem, song: PlaylistItemSongAndSong) {
        if (playlistItems != null) {
            var playItemPosition = 0
            for (item in playlistItems!!) {
                if (playlistItem.id == item.id) {
                    for (songItem in item.songs) {
                        if (songItem.item.id == song.item.id) {
                            item.songs.remove(songItem)

                            var position = 0
                            var hasPlayer: Boolean = false
                            // stop controller player of song of playing playlist item
                            for (player in players) {
                                if (player.getSong()!!.item.id == song.item.id) {
                                    if (player.isPlaying())
                                        player.stop()
                                    player.release()
                                    players.removeAt(position)
                                    hasPlayer = true
                                    break
                                }
                                position++
                            }
                            // - If item's deleted before the playing playlist => decresase songPosition 1 and delete playlistItem if songs size = 0
                            // - If item's deleted equal the playing playlist => decresase songPosition 1 and play next playlistItem if size of the playing playlist = 0
                            // - If item's deleted after the playing playlist => remove playlistItem if songs size = 0
                            if (playItemPosition < songPosition) {
                                songPosition--
                                if (item.songs.size == 0) {
                                    playlistItems!!.removeAt(playItemPosition)
                                }
                            } else if (playItemPosition == songPosition) {
                                if (item.songs.size == 0) {
                                    playlistItems!!.removeAt(playItemPosition)
                                    songPosition--
                                    playNext(false)
                                }
                            } else {
                                if (item.songs.size == 0) {
                                    playlistItems!!.removeAt(playItemPosition)
                                }
                            }
                            break
                        }
                    }
                    break
                }
                playItemPosition++
            }
            if (playlistItems!!.size == 0) {
                val intent = Intent(Constants.BROADCAST_PLAY_PLAYLIST)
                intent.putExtra(Constants.EXTRAX_HIDDEN_CONTROLLER, true)
                sendBroadcast(intent)
            }
        }
    }

    fun addSongToPlaylist(playlistItem: PlaylistItem) {
        if (playlistItems == null) {
            playlistItems = ArrayList()
        }
        if (playlistItems!!.size > 0) {
            if (playlistItems!!.get(0).playlistId == playlistItem.playlistId) {
                playlistItems!!.add(playlistItem)
            }
        }
    }

    fun clearPlaylist(playlistId: Long) {
        if (playlistItems == null) {
            playlistItems = ArrayList()
        }
        if (playlistItems!!.size > 0) {
            if (playlistItems!!.get(0).playlistId == playlistId) {
                //stop all player
                stopMusicService()
            }
        }
    }

    fun stopMusicService() {
        isPaused = false
        for (player in players) {
            if (player.isPlaying())
                player.stop()
            player.release()
        }
        players.clear()
        longestDurationPlayer = null
        //clear all callbacks
        for (callback in callbacks) {
            callback.onStopPlayback()
        }
        //Clear playlist
        songPosition = -1
        if (playlistItems != null) {
            playlistItems!!.clear()
        }
        //Hidden controller player
        val intent = Intent(Constants.BROADCAST_PLAY_PLAYLIST)
        intent.putExtra(Constants.EXTRAX_HIDDEN_CONTROLLER, true)
        sendBroadcast(intent)
    }

    private fun getDeviceVolume(): Float {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return maxVolume.toFloat()
    }

    private fun setCountDownTimerStart(song: PlaylistItemSongAndSong) {
        val volumeStep = getDeviceVolume() / 5
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(0F), 0)
        mCountDownTimerStart = object : CountDownTimer(5000, 1000) {
            override fun onFinish() {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(getDeviceVolume()), 0)
                cancelCountDownStart()
            }

            override fun onTick(p0: Long) {
                val totalSeconds = ((5000 - p0) / 1000).toInt()
                val volume = totalSeconds * volumeStep
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(volume), 0)
                Log.d("VOLUME", "fade in $volume")
            }
        }
        mCountDownTimerStart?.start()
    }

    fun setCountDownTimerEnd(song: PlaylistItemSongAndSong) {
        val volumeSpace = getDeviceVolume() / 5
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(getDeviceVolume()), 0)
        mCountDownTimerEnd = object : CountDownTimer(5000, 1000) {
            override fun onFinish() {
                if (song.item.endOffset == 0L) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(0f), 0)
                    cancelCountDownEnd()
                }
            }

            override fun onTick(p0: Long) {
                val totalSeconds = (p0 / 1000).toInt()
                val volume = totalSeconds * volumeSpace
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(volume), 0)
                Log.d("VOLUME", "fade out $volume")
            }
        }
        mCountDownTimerEnd?.start()
    }


    //skip to previous track
    fun playPrev() {
        if (playlistItems == null || (playlistItems != null && playlistItems!!.size == 0)) {
            return
        }
        if (shuffle) {
            var newSong = songPosition
            while (newSong == songPosition) {
                newSong = rand!!.nextInt(playlistItems!!.size)
            }
            songPosition = newSong
            playSong(false)
        } else {
//            if (repeatType == RepeatType.ALL) {
            songPosition--
            if (songPosition < 0) songPosition = playlistItems!!.size - 1
            playSong(false)
//            } else if (repeatType == RepeatType.NONE) {
//                if (songPosition > 0) {
//                    songPosition--
//                    playSong()
//                }
//            } else {
//                if (songPosition < 0 || songPosition > playlistItems!!.size - 1) {
//                    songPosition = 0
//                }
//                playSong()
//            }
        }
    }

    //skip to next
    fun playNext(autoNext: Boolean) {
        if (playlistItems == null || (playlistItems != null && playlistItems!!.size == 0)) {
            return
        }
        if (shuffle) {
            var newSong = songPosition
            while (newSong == songPosition) {
                newSong = rand!!.nextInt(playlistItems!!.size)
            }
            songPosition = newSong
            playSong(false)
        } else {
            //songPosition++
            if (repeatType == RepeatType.ALL || !autoNext) {
                songPosition++
                if (songPosition > playlistItems!!.size - 1) songPosition = 0
                playSong(false)
            } else if (repeatType == RepeatType.NONE) {
                if (songPosition < playlistItems!!.size - 1) {
                    songPosition++
                    playSong(false)
                }
            } else {
                if (songPosition < 0 || songPosition > playlistItems!!.size - 1) {
                    songPosition = 0
                }
                playSong(false)
            }
        }

        /*if (songPosition >= playlistItems!!.size) {
            songPosition = 0
            if (repeatType == RepeatType.ALL) {
                playSong()
            } else {
                for (callback in callbacks) {
                    isPlaying = false
                    callback.onStopPlayback()
                }
            }
        } else {
            playSong()
        }*/
    }

    fun getCurrentItems(): ArrayList<PlaylistItem>? {
        return playlistItems
    }

    fun getCurrentPlaylistItem(): PlaylistItem? {
        if (playlistItems != null && songPosition > -1 && songPosition < playlistItems!!.size) {
            return playlistItems!![songPosition]
        }
        return null
    }

    override fun onDestroy() {
        for (player in players) {
            player.stop()
            player.release()
        }
        stopForeground(true)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        this.stopSelf()
    }

    companion object {
        //notification id
        private val NOTIFY_ID = 1
    }

    interface Callback {
        fun onPlaylistItemPlayed(playlistItem: PlaylistItem)

        fun onPlaylistItemStart(playlistItem: PlaylistItem)

        fun onStopPlayback()

        fun onPausePlayback()

        fun updateDurationPlayer(duration: Int, currentDuration: Int, song: PlaylistItemSongAndSong)

        fun updateTitlePlayer(title: String, albumId: Long)//, songId: Long
    }

    interface IGetSongPlaying {
        fun songPlaying(songId: Long)
    }
}
