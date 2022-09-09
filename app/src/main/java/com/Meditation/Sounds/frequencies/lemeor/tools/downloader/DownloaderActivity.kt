package com.Meditation.Sounds.frequencies.lemeor.tools.downloader

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.tools.downloader.DownloadService.Companion.DOWNLOAD_FINISH
import com.Meditation.Sounds.frequencies.utils.Constants
import kotlinx.android.synthetic.main.activity_downloader.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DownloaderActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRACKS_LIST = "extra_tracks_list"
        private const val STORAGE_PERMISSION_CODE = 1001

        fun newIntent(context: Context?, tracks: ArrayList<Track>): Intent {
            val intent = Intent(context, DownloaderActivity::class.java)
            intent.putParcelableArrayListExtra(EXTRA_TRACKS_LIST, tracks)
            return intent
        }
    }

    private lateinit var mViewModel: DownloaderViewModel

    private var mDownloaderAdapter: DownloaderAdapter? = null
    private var tracks = ArrayList<Track>()

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Any?) {
        if (event?.javaClass == DownloadInfo::class.java) {
            val download = event as DownloadInfo

            updateUI(download)
        }

        if (event == DOWNLOAD_FINISH) {
            Constants.tracks.clear()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloader)

        EventBus.getDefault().register(this)

        initUI()

        if (intent != null) {

            tracks = intent.getParcelableArrayListExtra(EXTRA_TRACKS_LIST)!!
            if(Constants.tracks.size == 0)
            {
                Constants.tracks.addAll(tracks)
            }
            else {
                val difference = tracks.toSet().minus(Constants.tracks.toSet())
                Constants.tracks.addAll(difference)
            }
            mDownloaderAdapter?.setData(Constants.tracks)
            downloader_tv_quantity.text = getString(R.string.downloader_quantity, 1, Constants.tracks.size)
        }

        checkStoragePermission()
    }

    private fun initUI() {
        mViewModel = ViewModelProvider(this,
                ViewModelFactory(
                        ApiHelper(RetrofitBuilder(applicationContext).apiService),
                        DataBase.getInstance(applicationContext))
        ).get(DownloaderViewModel::class.java)

        downloader_btn_back.setOnClickListener { onBackPressed() }

        mDownloaderAdapter = DownloaderAdapter(applicationContext, Constants.tracks)
        downloader_recycler_view.adapter = mDownloaderAdapter
        downloader_recycler_view.setHasFixedSize(true)
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    private fun updateUI(download: DownloadInfo) {
        downloader_tv_quantity.text = getString(R.string.downloader_quantity, download.completed + 1, download.total)

        var position = 0
        Constants.tracks.forEachIndexed { i, t ->
            if (download.tag == t.id.toString()) { position = i }
        }
        mDownloaderAdapter?.updateProgress(position, download.progress)
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        } else {
            DownloadService.startService(this, Constants.tracks)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE || grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            DownloadService.startService(this, Constants.tracks)
        } else {
            Toast.makeText(applicationContext, "Need permission to download tracks.", Toast.LENGTH_SHORT).show()
            checkStoragePermission()
        }
    }
}