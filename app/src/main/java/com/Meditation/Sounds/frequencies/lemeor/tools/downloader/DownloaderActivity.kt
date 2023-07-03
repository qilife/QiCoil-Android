package com.Meditation.Sounds.frequencies.lemeor.tools.downloader

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.QApplication
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.downloadErrorTracks
import com.Meditation.Sounds.frequencies.lemeor.downloadedTracks
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloadService.Companion.DOWNLOAD_FINISH
import com.Meditation.Sounds.frequencies.utils.Constants
import kotlinx.android.synthetic.main.activity_downloader.*
import kotlinx.android.synthetic.main.activity_navigation.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DownloaderActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRACKS_LIST = "extra_tracks_list"
        const val EXTRA_VIEW_DOWNLOAD = "extra_view_download"
        private const val STORAGE_PERMISSION_CODE = 1001

        fun newIntent(
            context: Context?,
            tracks: ArrayList<Track>,
            isViewDownload: Boolean = false
        ): Intent {
            val intent = Intent(context, DownloaderActivity::class.java)
            intent.putParcelableArrayListExtra(EXTRA_TRACKS_LIST, tracks)
            intent.putExtra(EXTRA_VIEW_DOWNLOAD, isViewDownload)
            return intent
        }
    }

    private lateinit var mViewModel: DownloaderViewModel

    private var mDownloaderAdapter: DownloaderAdapter? = null
    private var tracks = ArrayList<Track>()
    private var isViewDownload = false

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Any?) {
        if (event?.javaClass == DownloadInfo::class.java) {
            val download = event as DownloadInfo

            updateUI(download)
        }

        if (event?.javaClass == DownloadErrorEvent::class.java && QApplication.isActivityDownloadStarted) {
            downloadedTracks = null
            downloadErrorTracks = null
            Constants.tracks.clear()
            androidx.appcompat.app.AlertDialog.Builder(this@DownloaderActivity)
                .setTitle(R.string.download_error)
                .setMessage(getString(R.string.download_error_message))
                .setPositiveButton(R.string.txt_ok,
                    DialogInterface.OnClickListener { p0, p1 -> this@DownloaderActivity.finish() })
                .show()
        }

        if (event == DOWNLOAD_FINISH) {
            Constants.tracks.clear()
            finish()
        }

        if (event?.javaClass == DownloadTrackErrorEvent::class.java) {
            mDownloaderAdapter?.notifyDataSetChanged()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloader)
        QApplication.isActivityDownloadStarted = true
        EventBus.getDefault().register(this)

        downloader_tv_quantity.text = ""

        initUI()

        if (intent != null) {
            isViewDownload = intent.getBooleanExtra(EXTRA_VIEW_DOWNLOAD, false)
            tracks = intent.getParcelableArrayListExtra(EXTRA_TRACKS_LIST)!!
            if (!isViewDownload) {
                if (Constants.tracks.size == 0) {
                    Constants.tracks.addAll(tracks)
                } else {
                    val difference = tracks.toSet().minus(Constants.tracks.toSet())
                    Constants.tracks.addAll(difference)
                }
            }
            val tmpTracks = ArrayList<Track>()
            Constants.tracks.forEach {
                if (!it.isDownloaded) {
                    tmpTracks.add(it)
                }
            }
            Constants.tracks.clear()
            Constants.tracks.addAll(tmpTracks)

            mDownloaderAdapter?.setData(Constants.tracks)

            downloader_tv_quantity.text =
                getString(R.string.downloader_quantity, 1, Constants.tracks.size)

            EventBus.getDefault().post(DownloadCountInfo(Constants.tracks.size))

        }

        checkStoragePermission()
    }

    private fun initUI() {
        mViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                ApiHelper(RetrofitBuilder(applicationContext).apiService),
                DataBase.getInstance(applicationContext)
            )
        ).get(DownloaderViewModel::class.java)

        downloader_btn_back.setOnClickListener { onBackPressed() }

        mDownloaderAdapter = DownloaderAdapter(applicationContext, Constants.tracks)
        downloader_recycler_view.adapter = mDownloaderAdapter
        downloader_recycler_view.setHasFixedSize(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        QApplication.isActivityDownloadStarted = false
        EventBus.getDefault().unregister(this)
    }

    private fun updateUI(download: DownloadInfo) {
        downloader_tv_quantity.text =
            getString(R.string.downloader_quantity, download.completed + 1, download.total)

        var position = 0
        Constants.tracks.forEachIndexed { i, t ->
            if (download.tag == t.id.toString()) {
                position = i
            }
        }
        mDownloaderAdapter?.updateProgress(position, download.progress)
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        } else {
            DownloadService.startService(this, Constants.tracks)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE || grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            DownloadService.startService(this, Constants.tracks)
        } else {
            Toast.makeText(
                applicationContext,
                "Need permission to download tracks.",
                Toast.LENGTH_SHORT
            ).show()
            checkStoragePermission()
        }
    }
}