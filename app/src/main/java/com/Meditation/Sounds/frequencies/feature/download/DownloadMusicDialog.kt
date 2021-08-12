package com.Meditation.Sounds.frequencies.feature.download

import android.app.Dialog
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Window
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.feature.base.BaseActivity
import com.Meditation.Sounds.frequencies.models.Album
import com.Meditation.Sounds.frequencies.tasks.BaseTask
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.FileUtils
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.Meditation.Sounds.frequencies.utils.StringUtils
import kotlinx.android.synthetic.main.dialog_download_music.*
import java.io.File
import java.lang.Exception
import java.net.URLDecoder
import java.util.concurrent.Executors

class DownloadMusicDialog(private var activity: BaseActivity, private var albums: ArrayList<Album>, private var callback: Callback?) : Dialog(activity), DownloadFileTask.Callback, ApiListener<Any> {

    private val executor = Executors.newFixedThreadPool(3)
    private var downloadedFile = 0
    private var totalDownloadFile = 0
    private var mAdapter: DownloadAdapter? = null
    val CACHE_FOLDER = File(FileUtils.getSdcardStore(), Constants.DEFAULT_DATA_FOLDER)
    val CACHE_FOLDER_ADVANCED = File(FileUtils.getSdcardStore(), Constants.DEFAULT_DATA_ADVANCED_FOLDER)
    val CACHE_FOLDER_ABUNDANCE = File(FileUtils.getSdcardStore(), Constants.DEFAULT_DATA_ABUNDANCE_FOLDER)
    val CACHE_FOLDER_HIGHER_QUANTUM = File(FileUtils.getSdcardStore(), Constants.DEFAULT_DATA_HIGHER_QUANTUM_FOLDER)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_download_music)
        if (!CACHE_FOLDER.exists()) {
            CACHE_FOLDER.mkdir()
        }
        if (!CACHE_FOLDER_ADVANCED.exists()){
            CACHE_FOLDER_ADVANCED.mkdir()
        }
        if (!CACHE_FOLDER_ABUNDANCE.exists()){
            CACHE_FOLDER_ABUNDANCE.mkdir()
        }
        if (!CACHE_FOLDER_HIGHER_QUANTUM.exists()){
            CACHE_FOLDER_HIGHER_QUANTUM.mkdir()
        }
        setCancelable(false)
        list.layoutManager = LinearLayoutManager(activity)
        mAdapter = DownloadAdapter(ArrayList())
        list.adapter = mAdapter

        for (album in albums) {
            addToDownload(album, true)
        }
        this@DownloadMusicDialog.description.text = this@DownloadMusicDialog.activity.getText(R.string.txt_downloading).toString()
                .plus(" (").plus(downloadedFile).plus("/").plus(totalDownloadFile).plus(")")
    }

    fun addToDownload(album : Album, isDownloadArts : Boolean){
        val albumFolder = File(when{
            album.album_type == 1->CACHE_FOLDER_ADVANCED
            album.album_type == 2->CACHE_FOLDER_ABUNDANCE
            album.album_type == 3->CACHE_FOLDER_HIGHER_QUANTUM
            else ->CACHE_FOLDER
        }, album.name)//if (album.album_type == 1) CACHE_FOLDER_ADVANCED else CACHE_FOLDER
        if (!albumFolder.exists()) {
            albumFolder.mkdir()
        }
        if (!TextUtils.isEmpty(album.albumArt)) {
            val file = File(albumFolder, Constants.ALBUM_ART_FILE_NAME)
            //File is encoded
            val fileWithoutExtension = File(albumFolder, StringUtils.getFileNameWithoutExtension(file.name))
            if (!file.exists() && !fileWithoutExtension.exists() && isDownloadArts) {
                DownloadFileTask(album, album.albumArt!!, 0, false, this@DownloadMusicDialog).executeOnExecutor(executor)
                totalDownloadFile++
            }
        }
        for (song in album.songUrls) {
            val fileName = URLDecoder.decode(StringUtils.getFileName(song), Constants.CHARSET)
            //File is encoded
            val fileNameWithoutExtension = StringUtils.getFileNameWithoutExtension(fileName)
            val fileEncrypt = fileNameWithoutExtension + "." + Constants.EXTENSION_ENCRYPT_FILE
            if (!File(albumFolder, fileName).exists() && !File(albumFolder, fileEncrypt).exists() && !File(albumFolder, fileNameWithoutExtension).exists()) {
                DownloadFileTask(album, song, album.songUrls.indexOf(song) + 1, true, this@DownloadMusicDialog).executeOnExecutor(executor)
                totalDownloadFile++
            }
        }
    }

    fun additionDownloadData(additionAlbums : ArrayList<Album>){

        for (item in additionAlbums){
            var added = false
            for (album in albums){
                if(item.name.equals(album.name, ignoreCase = true) && album.songUrls != null && album.songUrls.size > 0){
                    added = true
                    break
                }
            }
            if(!added){
                addToDownload(item, false)
            }
        }
        this@DownloadMusicDialog.description.text = this@DownloadMusicDialog.activity.getText(R.string.txt_downloading).toString()
                .plus(" (").plus(downloadedFile).plus("/").plus(totalDownloadFile).plus(")")
    }

    override fun onConnectionOpen(task: BaseTask<*>?) {

    }

    override fun onConnectionSuccess(task: BaseTask<*>?, data: Any?) {

    }

    override fun onConnectionError(task: BaseTask<*>?, exception: Exception?) {

    }

    override fun onPreExecute(downloadItem: DownloadItem) {
        mAdapter!!.data.add(downloadItem)
        mAdapter!!.notifyItemInserted(mAdapter!!.data.indexOf(downloadItem))
    }

    override fun onError(throwable: Throwable) {
        if (isShowing) {
            try {
                this@DownloadMusicDialog.dismiss()
            } catch (e : IllegalArgumentException) {
            }
            executor.shutdownNow()
            this@DownloadMusicDialog.callback?.onError(throwable)
        }
    }

    override fun onSuccess(downloadItem: DownloadItem) {
        val index = mAdapter!!.data.indexOf(downloadItem)
        if (index >= 0) {
            mAdapter!!.data.remove(downloadItem)
            mAdapter!!.notifyItemRemoved(index)
        }
        downloadedFile++
        this@DownloadMusicDialog.description.text = this@DownloadMusicDialog.activity.getText(R.string.txt_downloading).toString()
                .plus(" (").plus(downloadedFile).plus("/").plus(totalDownloadFile).plus(")")
        if (downloadedFile == totalDownloadFile) {
            SharedPreferenceHelper.getInstance().setBool(Constants.IS_DOWNLOADED_ALL_ALBUM, true)
            this@DownloadMusicDialog.dismiss()
            this@DownloadMusicDialog.callback?.onSuccess()
        }
    }

    override fun onProgressUpdate(downloadItem: DownloadItem) {
        val index = mAdapter!!.data.indexOf(downloadItem)
        if (index >= 0) {
//            mAdapter!!.notifyItemChanged(index)
        }
        mAdapter!!.notifyDataSetChanged()
    }

    interface Callback {
        fun onSuccess()
        fun onError(throwable: Throwable)
    }

}
