package com.Meditation.Sounds.frequencies

import android.media.MediaPlayer
import android.os.CountDownTimer
import android.util.Log
import com.Meditation.Sounds.frequencies.models.PlaylistItemSongAndSong

class ExtendMediaPlayer:MediaPlayer(),  MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener{
    private var extendErrorListener:MediaPlayer.OnErrorListener? = null

    private var extendPreparedListener:MediaPlayer.OnPreparedListener? = null

    private var extendOnCompletionListener: MediaPlayer.OnCompletionListener? = null

    var isCompleted = false
    private set

    var isPrepared = false
    private set

    var isPreparing = false
        private set

    var isError = false
    private set

    var isPauseImmediatelyStarted = false
        set

    private var song:PlaylistItemSongAndSong? = null

    private var countDownTimer:CountDownTimer? = null

    init {
        super.setOnCompletionListener(this)
        super.setOnErrorListener(this)
        super.setOnPreparedListener(this)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        mp?.reset()
        isCompleted = true
        extendOnCompletionListener?.onCompletion(mp)
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        isError = true
        extendErrorListener?.onError(mp, what, extra)
        return false
    }

    override fun onPrepared(mp: MediaPlayer?) {
        isPrepared = true
        extendPreparedListener?.onPrepared(mp)
        isPreparing = false
    }

    override fun reset() {
        super.reset()
        isError = false
        isPrepared = false
        isCompleted = false
        isPreparing = false
    }


    override fun setOnPreparedListener(listener: OnPreparedListener?) {
        super.setOnPreparedListener(this)
        extendPreparedListener = listener
    }

    override fun setOnCompletionListener(listener: OnCompletionListener?) {
        super.setOnCompletionListener(this)
        extendOnCompletionListener = listener
    }

    override fun setOnErrorListener(listener: OnErrorListener?) {
        super.setOnErrorListener(this)
        extendErrorListener = listener
    }

    fun play(song: PlaylistItemSongAndSong){
        this.song = song
        //get id
        //set the data source
        try {
            setDataSource(song.song.path)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }

//        setVolume(song.item.volumeLevel, song.item.volumeLevel)

        prepareAsync()
    }

    override fun release() {
        super.release()
        isPreparing = false
        stopTimer()
    }

    override fun stop() {
        super.stop()
        isPreparing = true
        stopTimer()
    }

    override fun pause() {
        try {
            super.pause()
            isPreparing = false
            stopTimer()
        }catch (ex: IllegalStateException){}
    }

    override fun start() {
        isCompleted = false
        stopTimer()
        if(song!=null){
            startTimer(currentPosition)
//            seekTo(song!!.item.startOffset.toInt())
//            super.start()
        }else{
            super.start()
        }
    }

    fun startFromResume(){
        super.start()
    }

    override fun seekTo(msec: Int) {
        super.seekTo(msec)
        stopTimer()
        startTimer(msec)
    }

    fun startTimer(currentDuration : Int){
        if(song!!.item.endOffset > currentDuration) {
            countDownTimer = object: CountDownTimer(song!!.item.endOffset - currentDuration, song!!.item.endOffset - currentDuration){
                override fun onFinish() {
                    pause()
                    onCompletion(this@ExtendMediaPlayer)
                }

                override fun onTick(time: Long) {

                }
            }
            countDownTimer!!.start()
            super.start()
        }else{
            onCompletion(this)
        }
    }

    private fun stopTimer(){
        countDownTimer?.cancel()
        countDownTimer = null
    }

    fun getSong() : PlaylistItemSongAndSong? {
        return song
    }
}

