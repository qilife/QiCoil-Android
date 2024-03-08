package com.Meditation.Sounds.frequencies.lemeor.tools.downloader

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.TrackDao
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.getSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getTrackUrl
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.codeLanguage
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.work.DownLoadCourseAudioWorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File
import kotlin.collections.set


class DownloadService : LifecycleService() {

    companion object {
        private const val CHANNEL_ID = "DownloadService"
        private const val EXTRA_TRACKS = "extra_tracks"
        const val DOWNLOAD_FINISH = "download_finish"

        fun startService(context: Context, tracks: ArrayList<Track>) {
            val startIntent = Intent(context, DownloadService::class.java)
            startIntent.putParcelableArrayListExtra(EXTRA_TRACKS, tracks)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, DownloadService::class.java)
            context.stopService(stopIntent)
        }
    }

    var tracks = mutableListOf<Track>()
        private set

    var downloadErrorTracks = HashSet<Int>()
        private set

    val fileProgressMap: HashMap<Int, Int> = HashMap()
    private var trackDao: TrackDao? = null

    // Binder given to clients.
    private val binder = DownloadServiceBinder()

    private var hasNetwork = true
    private var isDownloading = false

    private val networkChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val isNetworkAvailable = Utils.isConnectedToNetwork(context)
            if (hasNetwork != isNetworkAvailable) {
                hasNetwork = isNetworkAvailable
                if (hasNetwork) {
                    downloadErrorTracks.clear()
                    downloadNext(true)
                }
            }
        }

    }

    override fun onCreate() {
        super.onCreate()
        WorkManager.getInstance(application).cancelAllWorkByTag(DownLoadCourseAudioWorkManager.TAG)
        trackDao = DataBase.getInstance(applicationContext).trackDao()
        hasNetwork = Utils.isConnectedToNetwork(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                networkChangeReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
                Context.RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(
                networkChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.tv_downloading_file))
            .setSmallIcon(R.mipmap.ic_launcher).build()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }

        val tracks: List<Track> = intent?.getParcelableArrayListExtra(EXTRA_TRACKS)!!
        val needToDownloadTrack =
            this.tracks.filter { downloadErrorTracks.contains(it.id) }.toMutableList()
        this.tracks = this.tracks.filter { !downloadErrorTracks.contains(it.id) }.toMutableList()
        tracks.forEach {
            if (!this.tracks.any { track -> track.id == it.id } && !downloadErrorTracks.contains(it.id)) {
                needToDownloadTrack.add(it)
            }
        }
        downloadErrorTracks.clear()
        if (getCompletedFileCount() == this.tracks.size) {
            this.tracks.clear()
            fileProgressMap.clear()
        }
        this.tracks.addAll(needToDownloadTrack)
        val dao = DataBase.getInstance(applicationContext).albumDao()
        CoroutineScope(Dispatchers.IO).launch {
            needToDownloadTrack.forEach { t ->
                if (t.album == null) {
                    val album = dao.getAlbumById(t.albumId, t.category_id)
                    t.album = album
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                enqueueFiles(needToDownloadTrack)

                EventBus.getDefault().post(
                    DownloadInfo(
                        "", 0, getCompletedFileCount(), this@DownloadService.tracks.size
                    )
                )
                if (this@DownloadService.tracks.isEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } else {
                        stopForeground(true)
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Download Service Channel", NotificationManager.IMPORTANCE_LOW
            ).apply {
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }


    override fun onDestroy() {
        downloadErrorTracks.clear()
        WorkManager.getInstance(application).cancelAllWorkByTag(DownLoadCourseAudioWorkManager.TAG)
        unregisterReceiver(networkChangeReceiver)
        super.onDestroy()
    }

    private fun updateUIWithProgress() {
        val totalFiles: Int = tracks.size
        val completedFiles: Int = getCompletedFileCount()

        if (completedFiles == totalFiles) {
            val message = when (PreferenceHelper.preference(applicationContext).codeLanguage) {
                "es" -> getString(R.string.tst_download_successful_es)
                "fr" -> getString(R.string.tst_download_successful_fr)
                "zh" -> getString(R.string.tst_download_successful_zh)
                else -> getString(R.string.tst_download_successful_en)
            }

            Toast.makeText(
                applicationContext, message, Toast.LENGTH_SHORT
            ).show()

            EventBus.getDefault().post(DOWNLOAD_FINISH)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                stopForeground(true)
            }
        }
    }

    private fun checkDoneDownloaded() {
        val totalFiles: Int = tracks.size
        val completedFiles: Int = getCompletedFileCount()
        if ((completedFiles + downloadErrorTracks.size) == totalFiles && downloadErrorTracks.size > 0) {
            EventBus.getDefault().post(DownloadErrorEvent(downloadErrorTracks.size))
        }
    }

    fun getCompletedFileCount(): Int {
        return tracks.filter {
            File(getSaveDir(applicationContext, it.filename, it.album?.audio_folder ?: "")).exists()
        }.size
    }

    private fun enqueueFiles(tracks: List<Track>) {
        tracks.forEach {
            if (File(
                    getSaveDir(
                        applicationContext, it.filename, it.album?.audio_folder ?: ""
                    )
                ).exists()
            ) {
                fileProgressMap[it.id] = 100
            }
        }
        if (!isDownloading) {
            downloadNext()
        }
    }

    private fun downloadNext(redownload: Boolean = false) {
        WorkManager.getInstance(application).cancelAllWorkByTag(DownLoadCourseAudioWorkManager.TAG)
        tracks.firstOrNull {
            (fileProgressMap[it.id] ?: 0) < 100 && (redownload || !downloadErrorTracks.contains(
                it.id
            ))
        }?.let {
            isDownloading = true
            downloadErrorTracks.remove(it.id)
            fileProgressMap[it.id] = 0
            val request = DownLoadCourseAudioWorkManager.start(applicationContext, it, it.album)
            observeWorker(request)
        } ?: run {
            isDownloading = false
        }

    }

    private fun observeWorker(request: OneTimeWorkRequest) {
        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(request.id)
            .observe(this) { workInfo ->
                CoroutineScope(Dispatchers.Main).launch {
                    val wasSuccess = workInfo.state == WorkInfo.State.SUCCEEDED
                    if (wasSuccess) {
                        val trackId =
                            workInfo.outputData.getInt(DownLoadCourseAudioWorkManager.TRACK_ID, 0)
                        fileProgressMap[trackId] = 100
                        downloadErrorTracks.remove(trackId)
                        checkDoneDownloaded()
                        updateUIWithProgress()
                        EventBus.getDefault().post(
                            DownloadInfo(
                                "$trackId", 100, getCompletedFileCount(), tracks.size
                            )
                        )
                        downloadNext(false)
                    } else if (workInfo.state == WorkInfo.State.FAILED) {
                        workInfo.tags.firstOrNull { it.startsWith("track") }?.let { tag ->
                            val trackId = tag.replace("track_", "").toInt()
//                            if (!downloadErrorTracks.any { trackId == it }) {
                            tracks.firstOrNull { trackId == it.id }?.let {
                                downloadErrorTracks.add(it.id)
                                EventBus.getDefault().post(
                                    DownloadTrackErrorEvent(
                                        trackId, getTrackUrl(it.album, it.filename)
                                    )
                                )
//                                }
                            }
                            checkDoneDownloaded()
                        }
                        downloadNext()
                    } else if (workInfo != null) {
                        val total =
                            workInfo.progress.getLong(DownLoadCourseAudioWorkManager.TOTAL, 0L)
                        val downloaded =
                            workInfo.progress.getLong(DownLoadCourseAudioWorkManager.DOWNLOADED, 0L)
                        if (total > 0) {
                            val trackId =
                                workInfo.progress.getInt(DownLoadCourseAudioWorkManager.TRACK_ID, 0)
                            val progress = (downloaded * 100 / total).toInt();
                            fileProgressMap[trackId] = progress
                            EventBus.getDefault().post(
                                DownloadInfo(
                                    "$trackId", progress, getCompletedFileCount(), tracks.size
                                )
                            )
                        }
                    }
                }
            }
    }

    inner class DownloadServiceBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): DownloadService = this@DownloadService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }
}
