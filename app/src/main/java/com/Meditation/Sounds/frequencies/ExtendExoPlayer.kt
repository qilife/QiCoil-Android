package com.Meditation.Sounds.frequencies

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import com.Meditation.Sounds.frequencies.models.PlaylistItemSongAndSong
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.StringsUtils
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.File

class ExtendExoPlayer(var context: Context) {

    private var extendErrorListener: MediaPlayer.OnErrorListener? = null
    private var extendPreparedListener: MediaPlayer.OnPreparedListener? = null
    private var extendOnCompletionListener: MediaPlayer.OnCompletionListener? = null
    private var dataSourceFactory: DefaultDataSourceFactory
    private lateinit var mediaSource: MediaItem
    private var player: SimpleExoPlayer

    var isCompleted = false
        private set

    var isPrepared = false
        private set

    var isPreparing = false
        private set

    var isError = false
        private set

    var isStarted = false
    var ratioDiv = 0L

    var isPauseImmediatelyStarted = false
        set

    private var song: PlaylistItemSongAndSong? = null

    private var countDownTimer: CountDownTimer? = null

    var isCountDonwnEndRunning = false
    lateinit var mAudioManager: AudioManager
    var totalTimer: Long = 2000

    var mCountDownTimerStart: CountDownTimer? = null
    var mCountDownTimerEnd: CountDownTimer? = null
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

    init {
        player = SimpleExoPlayer.Builder(context).build()
        dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "ExoPlayerInfo"))

        mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (Constants.CURRENT_VOLUME <0){
            Constants.CURRENT_VOLUME = getCurrentVolume()
        }
    }

    fun play(song: PlaylistItemSongAndSong) {
        this.song = song
        //get id & set the data source
        try {
//            Toast.makeText(context, song.song.path, Toast.LENGTH_SHORT).show()
            val newFile = song.song.path?.let { FileEncyptUtil.renameToMp3File(it) }
            mediaSource = MediaItem.fromUri(Uri.fromFile(File(newFile)))
            player.addListener(object : Player.Listener {

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (player.playbackState == ExoPlayer.STATE_ENDED) {

                        ratioDiv++
                        play(song)
                    } else if (player.playbackState == ExoPlayer.STATE_READY) {
                        if (!isPrepared) {
                            isPrepared = true
                            extendPreparedListener?.onPrepared(MediaPlayer())
                            setCountDownTimerStart()
                            isPreparing = false
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    isError = true
                    extendErrorListener?.onError(MediaPlayer(), 0, 0)
                }
            })
            player.setMediaItem(mediaSource)
            player.playWhenReady = true
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }
    }


    fun onCompletion(mp: MediaPlayer?) {
//        mp?.reset()
        isCompleted = true
        extendOnCompletionListener?.onCompletion(mp)
    }

    fun isPlaying(): Boolean {
        return player != null
                && player.getPlaybackState() != Player.STATE_ENDED
                && player.getPlaybackState() != Player.STATE_IDLE
                && player.getPlayWhenReady();
    }

    fun currentPosition(): Int {
        return player.currentPosition.toInt()
    }

    fun duration(): Int {
        return player.duration.toInt()
    }

    fun reset() {
        isError = false
        isPrepared = false
        isCompleted = false
        isPreparing = false
    }


    fun setOnPreparedListener(listener: MediaPlayer.OnPreparedListener?) {
        extendPreparedListener = listener
    }

    fun setOnCompletionListener(listener: MediaPlayer.OnCompletionListener?) {
        extendOnCompletionListener = listener
    }

    fun setOnErrorListener(listener: MediaPlayer.OnErrorListener?) {
        extendErrorListener = listener
    }

    fun release() {
        player.release()
        isPreparing = false
        stopTimer()
        var pathWithoutExtension = StringsUtils.getFileNameWithoutExtension(song?.song?.path)
        FileEncyptUtil.renameToEncryptFile(pathWithoutExtension + "." + Constants.EXTENSION_MP3_FILE)
    }

    fun stop() {
        player.stop()
        isPreparing = true
        stopTimer()
    }

    fun pause() {
        player.playWhenReady = false
        isPreparing = false
        stopTimer()
    }

    fun start() {
        isCompleted = false
        stopTimer()
        if (!isStarted && song != null) {
            startTimer(player.currentPosition.toInt())
        } else {
            player.playWhenReady = true
        }
        isStarted = true
    }

    fun startFromResume() {
        start()
    }

    fun seekTo(msec: Int) {
        ratioDiv = msec.toLong() / this.duration()
        player.seekTo(msec.toLong() % this.duration())
        if (msec < totalTimer) {
            cancelCountDownStart()
            setCountDownTimerStart()
        }
        stopTimer()
        startTimer(msec)
    }

    fun resetDuration(endOffset: Long) {
        song!!.item.endOffset = endOffset
        startTimer((ratioDiv.toInt() * this.duration()) + currentPosition())
    }

    fun startTimer(currentDuration: Int) {
        if (song!!.item.endOffset > currentDuration) {
            if (countDownTimer != null) {
                isCountDonwnEndRunning = false
                cancelCountDownEnd()
                countDownTimer?.cancel()
            }
            countDownTimer = object : CountDownTimer(song!!.item.endOffset - currentDuration, 1000) {
                override fun onFinish() {
                    pause()
                    onCompletion(MediaPlayer())
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (Constants.CURRENT_VOLUME), 0)
                }

                override fun onTick(time: Long) {
                    if (time < (totalTimer + 1000)) {
                        Log.d("TOTAL TIME ", "time " + time)
                        if (time <= totalTimer) {
                            setCountDownTimerEnd()
                        } else {
                            setCountDownTimerEnd()
                        }
                    }

                    if ((song!!.item.endOffset - currentDuration - time) > totalTimer && time > totalTimer){
                        Constants.CURRENT_VOLUME = getCurrentVolume()
                    }
                }
            }
            countDownTimer!!.start()
            player.playWhenReady = true
        } else {
            onCompletion(MediaPlayer())
        }
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    fun getSong(): PlaylistItemSongAndSong? {
        return song
    }

    fun setVolume(volume: Float) {
        player.volume = volume
    }

    private fun getDeviceVolume(): Int {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return maxVolume
    }

    private fun getCurrentVolume(): Int {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return currentVolume
    }

    //fade in
    private fun setCountDownTimerStart() {
        Log.d("VOLUME", "currentVolumes " + Constants.CURRENT_VOLUME)
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(0F), 0)
        mCountDownTimerStart = object : CountDownTimer(totalTimer, totalTimer / 15) {
            override fun onFinish() {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Constants.CURRENT_VOLUME, 0)
                Log.d("VOLUME", "fade in finish " + getCurrentVolume())
                cancelCountDownStart()
            }

            override fun onTick(p0: Long) {
                val volume = Constants.CURRENT_VOLUME * (1 - ((p0 * 1.0F) / totalTimer))
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(volume), 0)
                Log.d("VOLUME", "fade in " + Math.round(volume))
                Log.d("VOLUME", "P0 $p0")
            }
        }
        mCountDownTimerStart?.start()
    }

    //fade out
    fun setCountDownTimerEnd() {
        if (!isCountDonwnEndRunning) {
            isCountDonwnEndRunning = true
        } else {
            return
        }
        Log.d("VOLUME", "currentVolumes " + Constants.CURRENT_VOLUME)

//        currentVolumes = getCurrentVolume()
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Constants.CURRENT_VOLUME, 0)
        mCountDownTimerEnd = object : CountDownTimer(totalTimer, totalTimer / 15) {
            override fun onFinish() {
                isCountDonwnEndRunning = false
                Log.d("VOLUME", "fade out finish " + getCurrentVolume())
                cancelCountDownEnd()
            }

            override fun onTick(p0: Long) {
                val volume = Constants.CURRENT_VOLUME * ((p0 * 1.0f) / totalTimer)
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(volume), 0)
                Log.d("VOLUME", "fade out " + Math.round(volume))
                Log.d("VOLUME", "P0 $p0")
            }
        }
        mCountDownTimerEnd?.start()
    }
}
