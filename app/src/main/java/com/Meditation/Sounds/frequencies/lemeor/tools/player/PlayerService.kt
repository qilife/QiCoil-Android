package com.Meditation.Sounds.frequencies.lemeor.tools.player

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.*
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.app.NotificationCompat.*
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.generators.SoundGenerator
import com.Meditation.Sounds.frequencies.generators.model.WaveTypes
import com.Meditation.Sounds.frequencies.lemeor.*
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.utils.Utils
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.REPEAT_MODE_ALL
import com.google.android.exoplayer2.Player.REPEAT_MODE_OFF
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.*


class PlayerService : Service() {

    companion object {
        var musicRepository: MusicRepository<Any>? = null
        const val NOTIFICATION_ID = 404
        const val NOTIFICATION_DEFAULT_CHANNEL_ID = "default_channel"
        const val EXTRA_PLAYLIST = "playlist"
    }

    private val soundFrequency = SoundGenerator().apply {
        init(44100)
        setWaveform(WaveTypes.SINUSOIDAL)
        setVolume(1F)
        setBalance(1F)
    }
    private val metadataBuilder = MediaMetadataCompat.Builder()


    private val stateBuilder = PlaybackStateCompat.Builder().setActions(
        PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
    )
    private var isRegisteredBusyReceiver = false

    private val mediaSession: MediaSessionCompat by lazy {
        val mediaButtonIntent = Intent(
            Intent.ACTION_MEDIA_BUTTON,
            null,
            applicationContext,
            MediaButtonReceiver::class.java
        )

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            mediaButtonIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        MediaSessionCompat(this, "PlayerService", null, pendingIntent).apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            setCallback(mediaSessionCallback)
        }
    }

    private val audioManager: AudioManager by lazy {
        getSystemService(AUDIO_SERVICE) as AudioManager
    }
    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioFocusRequested = false
    private var currentUri: Uri? = null
    var currentState = PlaybackStateCompat.STATE_STOPPED

    val exoPlayer: SimpleExoPlayer by lazy {
        SimpleExoPlayer.Builder(
            this,
            DefaultRenderersFactory(this),
        ).setLoadControl(DefaultLoadControl()).setTrackSelector(DefaultTrackSelector(this)).build()
    }
    private var progressTimer = Timer()
    private var isShuffle: Boolean = false

    private var playPosition: Long = 0
    private var isRepeatAll = false
    private var typePlayer = 1
    private var timerPlayRife = 3 * 60 * 1000L

    private var totalPlayedSoundTime = 0L
    private var startedPlaySoundTime = 0L
    private var timePlayed = 0L

    private var seconds = 0L

    private val trackListService = mutableListOf<MusicRepository.Music>()

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Any?) {
        if (event?.javaClass == PlayerShuffle::class.java) {
            val shuffle = event as PlayerShuffle
            isShuffle = shuffle.it
        }

        if (event?.javaClass == PlayerRepeat::class.java) {
            val repeat = event as PlayerRepeat
            var type = repeat.type
            typePlayer = repeat.type
            if (type == REPEAT_MODE_ALL) {
                isRepeatAll = true
                type = REPEAT_MODE_OFF
            } else {
                isRepeatAll = false
            }

            exoPlayer.repeatMode = type
        }

        if (event?.javaClass == PlayerSeek::class.java) {
            val seek = event as PlayerSeek
            val seekPosition = seek.position

//            if (seekPosition != null) {
//                mPart = ceil(seekPosition / 300000.0).toInt()
//            }
//
//            if (seekPosition != null) {
//                if (seekPosition > 300000) {
//                    seekPosition -= ((mPart - 1) * 300000)
//                }
//            }

            //todo remake this ugly fading
            exoPlayer.volume = 0F
            Thread.sleep(100)
            exoPlayer.seekTo(seekPosition.toLong())
            Thread.sleep(100)
            mediaSession.setPlaybackState(
                stateBuilder.setState(
                    currentState,
                    exoPlayer.currentPosition,
                    1f
                ).build()
            )
            exoPlayer.volume = 1F
        }

        if (event?.javaClass == PlayerSelected::class.java) {
            val selected = event as PlayerSelected
            musicRepository?.currentItemIndex = selected.position?.minus(1)!!
            mediaSessionCallback.onSkipToNext()
        }
    }

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        //trackList = ArrayList()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val notificationChannel = NotificationChannel(
                    NOTIFICATION_DEFAULT_CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val notificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)

                @Suppress("DEPRECATION") val notification =
                    Builder(this, NOTIFICATION_DEFAULT_CHANNEL_ID)
                        .setContentTitle("")
                        .setNotificationSilent()
                        .setContentText("").build()

                startForeground(NOTIFICATION_ID, getNotification(currentState))
            } catch (_: Exception) {

            }

            val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            audioFocusRequest = AudioFocusRequest.Builder(AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(true)
                .setAudioAttributes(audioAttributes)
                .build()
        }

        exoPlayer.addListener(exoPlayerListener)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

        //send state
    }

    private fun sendData() {
        if (musicRepository != null) {
            val item = musicRepository?.getCurrent()
            progressTimer.schedule(object : TimerTask() {
                override fun run() {
                    CoroutineScope(Dispatchers.Main).launch {
                        if (item is MusicRepository.Track) {
                            var position = exoPlayer.currentPosition
                            if (position < 0) position = 0

                            currentPosition.postValue(position)

                            val dur = exoPlayer.duration
                            duration.postValue(((max.value ?: dur) - position).coerceAtLeast(0))
                        } else {
                            timePlayed =
                                totalPlayedSoundTime + SystemClock.elapsedRealtime() - startedPlaySoundTime
                            if (timerPlayRife - timePlayed < 0) {
                                startedPlaySoundTime = SystemClock.elapsedRealtime()
                                totalPlayedSoundTime = 0
                                if (typePlayer == REPEAT_MODE_OFF) {
                                    if (musicRepository?.isLastTrack() == true) {
                                        mediaSessionCallback.onStop()
                                    }
                                } else if (typePlayer == REPEAT_MODE_ALL) {
                                    mediaSessionCallback.onSkipToNext()
                                }
                            }
                            duration.postValue((timerPlayRife - timePlayed).coerceAtLeast(0))
                            currentPosition.postValue(timePlayed)
                            if (seconds / 1000 == 1L) {
                                playRife?.apply {
                                    this.playtime = playtimeRife - timePlayed / 1000
                                    EventBus.getDefault().post(this)
                                }
                                seconds %= 1000
                            } else {
                                seconds += 300
                            }
                        }
                    }
                }
            }, 0, 300)
        } else {
            progressTimer.cancel()
            progressTimer.purge()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        try {
            if (intent?.hasExtra(EXTRA_PLAYLIST) == true) {
                progressTimer.cancel()
                progressTimer.purge()
//            val trackList =
//                intent.getParcelableArrayListExtra<MusicRepository.Music>(EXTRA_PLAYLIST)
                trackList?.let {
                    this.trackListService.clear()
                    this.trackListService.addAll(it)
                    musicRepository = MusicRepository(this.trackListService)
                }
            }
        } catch (_: Exception) {
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
//        unregisterReceiver(becomingNoisyReceiver)
        progressTimer.cancel()
        progressTimer.purge()
        mediaSession.release()
        exoPlayer.release()
        soundFrequency.release()
    }

    private val mediaSessionCallback: MediaSessionCompat.Callback =
        object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                val track = musicRepository?.getCurrent()
                track?.let { item ->
                    if(item is Track){
                        soundFrequency.stopPlayback()
                        if (!exoPlayer.playWhenReady) {
                            try {
                                ContextCompat.startForegroundService(
                                    applicationContext,
                                    Intent(applicationContext, PlayerService::class.java)
                                )
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                                //applicationContext.stopService(Intent(applicationContext, PlayerService::class.java))
                            }
                            //check obj frequency and track
//                    mMultiPlay = track.multiplay

                            updateMetadataFromTrack(item)
                            prepareToPlay(item)

                            if (!audioFocusRequested) {
                                audioFocusRequested = true

                                val audioFocusResult: Int =
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        audioManager.requestAudioFocus(audioFocusRequest!!)
                                    } else {
                                        audioManager.requestAudioFocus(
                                            audioFocusChangeListener,
                                            STREAM_MUSIC,
                                            AUDIOFOCUS_GAIN
                                        )
                                    }

                                if (audioFocusResult != AUDIOFOCUS_REQUEST_GRANTED) return
                            }

                            mediaSession.isActive = true
                            if(!isRegisteredBusyReceiver) {
                                isRegisteredBusyReceiver = true
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    registerReceiver(
                                        becomingNoisyReceiver,
                                        IntentFilter(ACTION_AUDIO_BECOMING_NOISY),
                                        Context.RECEIVER_EXPORTED
                                    )
                                }else{
                                    registerReceiver(
                                        becomingNoisyReceiver,
                                        IntentFilter(ACTION_AUDIO_BECOMING_NOISY)
                                    )
                                }
                            }
                            exoPlayer.playWhenReady = true
                        }
                    }else {
                        exoPlayer.stop()
                        try {
                            ContextCompat.startForegroundService(
                                applicationContext,
                                Intent(applicationContext, PlayerService::class.java)
                            )
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            //applicationContext.stopService(Intent(applicationContext, PlayerService::class.java))
                        }
                        //check obj frequency and track
//                    mMultiPlay = track.multiplay

                        updateMetadataFromTrack(item)
                        prepareToPlay(item)

                        if (!audioFocusRequested) {
                            audioFocusRequested = true

                            val audioFocusResult: Int =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    audioManager.requestAudioFocus(audioFocusRequest!!)
                                } else {
                                    audioManager.requestAudioFocus(
                                        audioFocusChangeListener,
                                        STREAM_MUSIC,
                                        AUDIOFOCUS_GAIN
                                    )
                                }

                            if (audioFocusResult != AUDIOFOCUS_REQUEST_GRANTED) return
                        }

                        mediaSession.isActive = true
                        if(!isRegisteredBusyReceiver) {
                            isRegisteredBusyReceiver = true
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                registerReceiver(
                                    becomingNoisyReceiver,
                                    IntentFilter(ACTION_AUDIO_BECOMING_NOISY),
                                    Context.RECEIVER_EXPORTED
                                )
                            }else{
                                registerReceiver(
                                    becomingNoisyReceiver,
                                    IntentFilter(ACTION_AUDIO_BECOMING_NOISY)
                                )
                            }
                        }
                    }

                }

                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        playPosition,
                        1f
                    ).build()
                )
                currentState = PlaybackStateCompat.STATE_PLAYING
                refreshNotificationAndForegroundStatus(currentState)
            }

            override fun onPause() {
                if (exoPlayer.playWhenReady) {
                    playPosition = exoPlayer.currentPosition

                    exoPlayer.playWhenReady = false
                    if (isRegisteredBusyReceiver) {
                        isRegisteredBusyReceiver = false
                        unregisterReceiver(becomingNoisyReceiver)
                    }
                }
                soundFrequency.stopPlayback()
                progressTimer.cancel()
                progressTimer.purge()
                totalPlayedSoundTime += SystemClock.elapsedRealtime() - startedPlaySoundTime
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_PAUSED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
                currentState = PlaybackStateCompat.STATE_PAUSED
                refreshNotificationAndForegroundStatus(currentState)
            }

            override fun onStop() {
                if (exoPlayer.playWhenReady) {
                    soundFrequency.stopPlayback()
                    progressTimer.cancel()
                    progressTimer.purge()
                    exoPlayer.playWhenReady = false
                    if (isRegisteredBusyReceiver) {
                        isRegisteredBusyReceiver = false
                        unregisterReceiver(becomingNoisyReceiver)
                    }
                }
                if (audioFocusRequested) {
                    audioFocusRequested = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
                    } else {
                        audioManager.abandonAudioFocus(audioFocusChangeListener)
                    }
                }
                mediaSession.isActive = false
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_STOPPED,
                        exoPlayer.currentPosition,
                        1f
                    ).build()
                )
                currentState = PlaybackStateCompat.STATE_STOPPED
                refreshNotificationAndForegroundStatus(currentState)
                stopSelf()
            }

            override fun onSkipToNext() {
                totalPlayedSoundTime = 0
                playPosition = 0

                val track = if (isMultiPlay) {
                    musicRepository?.getCurrent()
                } else {
//                    mPart = 1
                    if (isShuffle) {
                        musicRepository?.getRandom()
                    } else {
                        musicRepository?.getNext()
                    }
                }

                if (track != null) {
                    updateMetadataFromTrack(track)
                    refreshNotificationAndForegroundStatus(currentState)
                    prepareToPlay(track)
                }
            }

            override fun onSkipToPrevious() {
                try {
                    totalPlayedSoundTime = 0
                    playPosition = 0
                    if (musicRepository != null) {
                        val track: MusicRepository.Music = if (isShuffle) {
                            musicRepository!!.getRandom() as MusicRepository.Music
                        } else {
                            musicRepository!!.getPrevious() as MusicRepository.Music
                        }

                        updateMetadataFromTrack(track)
                        refreshNotificationAndForegroundStatus(currentState)
                        prepareToPlay(track)
                    }
                } catch (_: Exception) {

                }
            }

            fun prepareToPlay(item: Any) {
                soundFrequency.stopPlayback()
                exoPlayer.pause()
                if (item is MusicRepository.Track) {
                    val file = File(
                        getSaveDir(
                            applicationContext, item.filename, item.album.audio_folder
                        )
                    )
                    val preloaded = File(
                        getPreloadedSaveDir(
                            applicationContext, item.filename, item.album.audio_folder
                        )
                    )

                    var uri: Uri? = null
                    if (file.exists()) {
                        uri = Uri.fromFile(file)
                    }

                    if (preloaded.exists()) {
                        uri = Uri.fromFile(preloaded)
                    }
                    if (uri == null) {
                        uri = Uri.parse(
                            getTrackUrl(item.album, item.filename)
                        )
                        if (!Utils.isConnectedToNetwork(applicationContext)) {
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.err_network_available),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    uri?.let {
                        if (currentUri?.toString() != uri.toString() || (exoPlayer.playbackState != PlaybackState.STATE_PAUSED && exoPlayer.playbackState != PlaybackState.STATE_PLAYING)) {
                            currentUri = uri

                            //todo remake this ugly fading
                            exoPlayer.volume = 0F
                            Thread.sleep(100)
                            buildMediaSource(uri).let { exoPlayer.setMediaSource(it) }
                            exoPlayer.prepare()
                            exoPlayer.seekTo(playPosition)
                            Thread.sleep(100)
                            exoPlayer.volume = 1F
                            duration.postValue(0)
                        } else {
                            exoPlayer.seekTo(playPosition)
                        }
                        exoPlayer.play()
                        if (!isRegisteredBusyReceiver) {
                            isRegisteredBusyReceiver = true
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                registerReceiver(
                                    becomingNoisyReceiver,
                                    IntentFilter(ACTION_AUDIO_BECOMING_NOISY),
                                    Context.RECEIVER_EXPORTED
                                )
                            } else {
                                registerReceiver(
                                    becomingNoisyReceiver, IntentFilter(ACTION_AUDIO_BECOMING_NOISY)
                                )
                            }
                        }
                        mediaSession.isActive = true
                        mediaSession.setPlaybackState(
                            stateBuilder.setState(
                                PlaybackStateCompat.STATE_PLAYING,
                                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                                1f
                            ).build()
                        )
                        currentState = PlaybackStateCompat.STATE_PLAYING
                        refreshNotificationAndForegroundStatus(currentState)
                    }
                } else if (item is MusicRepository.Frequency) {
                    if (musicRepository != null) {
                        playtimeRife = musicRepository!!.getTime(3)
                    }
                    mediaSession.isActive = true
                    mediaSession.setPlaybackState(
                        stateBuilder.setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                            1f
                        ).build()
                    )
                    currentState = PlaybackStateCompat.STATE_PLAYING
                    refreshNotificationAndForegroundStatus(currentState)
                    max.postValue(timerPlayRife)
                    soundFrequency.startPlayback()
                }
                progressTimer.cancel()
                progressTimer.purge()
                startedPlaySoundTime = SystemClock.elapsedRealtime()
                progressTimer = Timer()
                sendData()
            }

            private fun buildMediaSource(uri: Uri): MediaSource {
                val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                    applicationContext,
                    Util.getUserAgent(applicationContext, getString(R.string.app_name))
                )
                return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            }

            fun updateMetadataFromTrack(item: Any) {
                if (item is MusicRepository.Track) {
                    metadataBuilder.putBitmap(
                        MediaMetadataCompat.METADATA_KEY_ART,
                        BitmapFactory.decodeResource(resources, item.resId)
                    )
                    metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, item.title)
                    metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, item.artist)
                    metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, item.artist)
                    metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                        item.duration.takeIf { it > 0 } ?: exoPlayer.duration)
                    mediaSession.setMetadata(metadataBuilder.build())
                } else if (item is MusicRepository.Frequency) {
                    soundFrequency.frequency = item.frequency
                    metadataBuilder.putBitmap(
                        MediaMetadataCompat.METADATA_KEY_ART,
                        BitmapFactory.decodeResource(resources, item.resId)
                    )
                    metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, item.title)
                    metadataBuilder.putString(
                        MediaMetadataCompat.METADATA_KEY_ALBUM, item.frequency.toString()
                    )
                    metadataBuilder.putString(
                        MediaMetadataCompat.METADATA_KEY_ARTIST, item.frequency.toString()
                    )
                    metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
                    mediaSession.setMetadata(metadataBuilder.build())
                }

//                max.postValue(track.duration)
//                duration.postValue(track.duration)
            }
        }

    private val audioFocusChangeListener = OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AUDIOFOCUS_GAIN -> {
                if (!exoPlayer.playWhenReady && !isUserPaused) {
                    mediaSessionCallback.onPlay()
                }
                exoPlayer.volume = 1F
            }

            AUDIOFOCUS_LOSS,
            AUDIOFOCUS_LOSS_TRANSIENT -> mediaSessionCallback.onPause()

            AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> exoPlayer.volume = 0.5F
            else -> mediaSessionCallback.onPause()
        }
    }

    private val becomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Disconnecting headphones - stop playback
            if (ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                mediaSessionCallback.onPause()
            }
        }
    }

    private val exoPlayerListener: Player.Listener = object : Player.Listener {

        override fun onPlaybackStateChanged(state: Int) {
            super.onPlaybackStateChanged(state)
            if (state == ExoPlayer.STATE_ENDED) {
                if (musicRepository?.isLastTrack() == true && !isRepeatAll) {
                    mediaSessionCallback.onStop()
                } else {
                    mediaSessionCallback.onSkipToNext()
                }
            }else if (state == ExoPlayer.STATE_READY) {
                max.postValue(exoPlayer.duration)
                metadataBuilder.putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION,
                    exoPlayer.duration
                )
                mediaSession.setMetadata(metadataBuilder.build())
                startForeground(NOTIFICATION_ID, getNotification(currentState))
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            mediaSessionCallback.onStop()
        }

    }

    override fun onBind(intent: Intent?): IBinder {
        return PlayerServiceBinder(mediaSession)
    }

    class PlayerServiceBinder(private val mediaSession: MediaSessionCompat) : Binder() {
        val mediaSessionToken: MediaSessionCompat.Token
            get() = mediaSession.sessionToken
    }

    private fun refreshNotificationAndForegroundStatus(playbackState: Int) {
        try {
            when (playbackState) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    startForeground(NOTIFICATION_ID, getNotification(playbackState))
                }

                PlaybackStateCompat.STATE_PAUSED -> {
                    NotificationManagerCompat.from(this@PlayerService)
                        .notify(NOTIFICATION_ID, getNotification(playbackState))
                    stopForeground(false)
                }

                else -> {
                    stopForeground(true)
                }
            }
        } catch (_: Exception) {

        }
    }

    private fun getNotification(playbackState: Int): Notification {
        val builder: Builder = styleThis(this, mediaSession)
        builder.addAction(
            Action(
                android.R.drawable.ic_media_previous,
                getString(R.string.previous),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
            )
        )
        if (playbackState == PlaybackStateCompat.STATE_PLAYING)
            builder.addAction(
                Action(
                    android.R.drawable.ic_media_pause,
                    getString(R.string.pause),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )
                )
            )
        else
            builder.addAction(
                Action(
                    android.R.drawable.ic_media_play,
                    getString(R.string.play),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )
                )
            )
        builder.addAction(
            Action(
                android.R.drawable.ic_media_next,
                getString(R.string.next),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                )
            )
        )
        builder.setStyle(
            NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(1)
                .setShowCancelButton(true)
                .setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )
                .setMediaSession(mediaSession.sessionToken)
        ) // setMediaSession for Android Wear
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.color = ContextCompat.getColor(
            this,
            R.color.colorPrimaryDark
        ) // The whole background (in MediaStyle), not just icon background
        builder.setShowWhen(false)
        builder.setNotificationSilent()
        builder.priority = PRIORITY_HIGH
        builder.setChannelId(NOTIFICATION_DEFAULT_CHANNEL_ID)
        return builder.build()
    }
}
