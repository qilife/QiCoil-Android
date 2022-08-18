package com.Meditation.Sounds.frequencies.lemeor.tools.downloader

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.TrackDao
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.downloadedTracks
import com.Meditation.Sounds.frequencies.lemeor.getSaveDir
import com.Meditation.Sounds.frequencies.lemeor.getTrackUrl
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2.Fetch.Impl.setDefaultInstanceConfiguration
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2core.Downloader.FileDownloaderType
import com.tonyodev.fetch2core.isNetworkAvailable
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.collections.set


class DownloadService : Service() {

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

    private var rxFetch: Fetch? = null

    private var tracks = ArrayList<Track>()

    private val fileProgressMap: HashMap<Int, Int> = HashMap()
    private var trackDao: TrackDao? = null

    override fun onCreate() {
        super.onCreate()
        trackDao = DataBase.getInstance(applicationContext).trackDao()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Downloading files...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setNotificationSilent()
                .build()
        startForeground(1, notification)

        tracks = intent?.getParcelableArrayListExtra(EXTRA_TRACKS)!!

        init()

        val dao = DataBase.getInstance(applicationContext).albumDao()
        GlobalScope.launch {
            tracks.forEach { t->
                val album = dao.getAlbumById(t.albumId)
                t.album = album
            }

            CoroutineScope(Dispatchers.Main).launch { enqueueFiles() }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? { return null }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Download Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun init() {
        val fetchConfiguration: FetchConfiguration = FetchConfiguration.Builder(this)
                .enableRetryOnNetworkGain(true)
                .setProgressReportingInterval(300)
                .setHttpDownloader(getOkHttpDownloader())
                .build()
        setDefaultInstanceConfiguration(fetchConfiguration)

        rxFetch = Fetch.getDefaultInstance()
        rxFetch?.addListener(fetchListener)

        rxFetch!!.removeAll()
        rxFetch!!.cancelAll()
        fileProgressMap.clear()
    }

    private fun getOkHttpDownloader(): Downloader<*, *> {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
                .readTimeout(20000L, TimeUnit.MILLISECONDS) //increase read timeout as needed
                .connectTimeout(20000L, TimeUnit.MILLISECONDS) //increase connection timeout as needed
                .build()
        return OkHttpDownloader(okHttpClient,
                Downloader.FileDownloaderType.PARALLEL)
    }

    override fun onDestroy() {
        super.onDestroy()

        downloadedTracks = null

        rxFetch?.deleteAllInGroupWithStatus(tracks[0].name.hashCode(), listOf(
                Status.NONE, Status.QUEUED, Status.DOWNLOADING, Status.PAUSED, Status.CANCELLED,
                Status.FAILED, Status.REMOVED, Status.DELETED, Status.ADDED
        ))
        rxFetch?.removeListener(fetchListener)
        rxFetch?.close()
    }

    private fun updateUIWithProgress() {
        val totalFiles: Int = fileProgressMap.size
        val completedFiles: Int = getCompletedFileCount()

        if (completedFiles == totalFiles) {
            Toast.makeText(applicationContext, getString(R.string.tst_download_successful), Toast.LENGTH_SHORT).show()

            EventBus.getDefault().post(DOWNLOAD_FINISH)

            stopSelf()
        }
    }

    private fun getCompletedFileCount(): Int {
        var count = 0
        val ids: Set<Int> = fileProgressMap.keys
        for (id in ids) {
            val progress = fileProgressMap[id]!!
            if (progress == 100) { count++ }
        }
        return count
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun enqueueFiles() {
        if(isNetworkAvailable()) {
            val requestList: List<Request> = getRequests()

            requestList.forEach { it.groupId = tracks[0].name.hashCode() }

            rxFetch?.enqueue(requestList) { updatedRequests: List<Pair<Request, Error?>> ->
                for ((first) in updatedRequests) {
                    fileProgressMap[first.id] = 0
                    updateUIWithProgress()
                }
            }
        }
    }

    private val fetchListener: FetchListener = object : AbstractFetchListener() {
        override fun onCompleted(download: Download) {
            fileProgressMap[download.id] = download.progress
            updateUIWithProgress()
        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            super.onError(download, error, throwable)

            Toast.makeText(applicationContext, "Download error: " + error.throwable?.message, Toast.LENGTH_LONG).show()
        }

        override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
            super.onProgress(download, etaInMilliSeconds, downloadedBytesPerSecond)

            EventBus.getDefault().post(DownloadInfo(download.tag!!, download.progress, getCompletedFileCount(), fileProgressMap.size))

            if (download.progress == 100) {
                var position = -1
                downloadedTracks?.forEachIndexed { i, t ->
                    if (t.id == Integer.valueOf(download.tag!!)) { position = i }
                }
                if (position != -1) { downloadedTracks?.removeAt(position) }

                GlobalScope.launch { trackDao?.isTrackDownloaded(true, Integer.valueOf(download.tag!!)) }
            }

            fileProgressMap[download.id] = download.progress
            updateUIWithProgress()
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun getRequests(): List<Request> {
        val requests: MutableList<Request> = ArrayList()

        tracks.forEach { track->
            val url: String = getTrackUrl(track.album, track)
            val filePath: String = getSaveDir(applicationContext, track, track.album!!)
            val request = Request(url, filePath)
            request.priority = Priority.HIGH
            request.networkType = NetworkType.ALL
            request.autoRetryMaxAttempts = 5
            request.tag = track.id.toString()
            requests.add(request)
        }
        return requests
    }
}
