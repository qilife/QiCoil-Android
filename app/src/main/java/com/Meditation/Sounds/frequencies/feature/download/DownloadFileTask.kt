package com.Meditation.Sounds.frequencies.feature.download

import android.os.AsyncTask
import android.util.Log
import com.Meditation.Sounds.frequencies.models.Album
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.FilesUtils
import com.Meditation.Sounds.frequencies.utils.StringsUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLDecoder

class DownloadFileTask(var album: Album, var url: String, var index: Int, var isSong: Boolean, private var callback: Callback?) : AsyncTask<Void, Int, File>() {
    private var fileSize = 0L
    private var downloadItem: DownloadItem? = null
    private var exeption: Throwable? = null
    val CACHE_FOLDER = File(FilesUtils.getSdcardStore(), when {
        album.album_type == 1 -> Constants.DEFAULT_DATA_ADVANCED_FOLDER
        album.album_type == 2 -> Constants.DEFAULT_DATA_ABUNDANCE_FOLDER
        album.album_type == 3 -> Constants.DEFAULT_DATA_HIGHER_QUANTUM_FOLDER
        else -> Constants.DEFAULT_DATA_FOLDER
    })//if(album.album_type == 1) Constants.DEFAULT_DATA_ADVANCED_FOLDER else Constants.DEFAULT_DATA_FOLDER


    override fun onPostExecute(result: File?) {
        super.onPostExecute(result)
        if (exeption != null) {
            callback?.onError(exeption!!)

        } else {
            callback?.onSuccess(downloadItem!!)
        }
    }

    override fun onProgressUpdate(vararg values: Int?) {
        if (values[0] != null) {
            if (values[0] == 0) {
//                downloadItem = DownloadItem(url, 0)
//                callback?.onPreExecute(downloadItem!!)
            } else {
                downloadItem?.progress = values[0]!!
                callback?.onProgressUpdate(downloadItem!!)
            }

        }
        super.onProgressUpdate(*values)

    }

    override fun onPreExecute() {
        super.onPreExecute()
        downloadItem = DownloadItem(album.name, album.album_type, url, 0)
        callback?.onPreExecute(downloadItem!!)
    }

    override fun doInBackground(vararg p0: Void?): File? {
        publishProgress(0)

        var inputStream: BufferedInputStream? = null
        var ouputStream: FileOutputStream? = null
        val albumFolder = File(CACHE_FOLDER, album.name)
        if (!albumFolder.exists()) {
            albumFolder.mkdir()
        }
        val tempFile = File(albumFolder, "_tmp".plus(index))

        try {
            val u = URL(url.replace(" ", "%20"))
            val connection = u.openConnection()
            connection.connect()

            fileSize = connection.contentLength.toLong()
            inputStream = BufferedInputStream(connection.getInputStream())

            ouputStream = FileOutputStream(tempFile)

            val data = ByteArray(1024)
            var total: Long = 0
            var count: Int = 0
            do {
                if (!isCancelled) {
                    count = inputStream.read(data)
                    if (count > 0) {
                        total += count.toLong()
                        ouputStream.write(data, 0, count)
                        // publishing the progress....
                        val percent = Math.round(total * 100f / fileSize)
                        if (percent != downloadItem?.progress) {
                            publishProgress(percent)
                            //Log.d("Data % of " + StringsUtils.getFileName(url) + ": ", "$percent")
                        }
                    }
                }
            } while (count != -1)

            if (isSong) {
                val file = File(albumFolder, URLDecoder.decode(StringsUtils.getFileName(this.url), Constants.CHARSET))
                tempFile.renameTo(file)
//                    val song = Song()
//                    song.path = file.path
//                    song.albumId = album.id
//
//                    val metaRetriever = MediaMetadataRetriever()
//                    metaRetriever.setDataSource(song.path)
//                    song.artist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
//                    song.title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
//                    song.duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
//                    song.id = database.songDAO().insert(song)
            } else {
                //album art
                val file = File(albumFolder, Constants.ALBUM_ART_FILE_NAME)
                tempFile.renameTo(file)
            }
        } catch (e: Throwable) {
            exeption = e
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (ex: java.lang.Exception) {
                }
            }
            if (ouputStream != null) {
                try {
                    ouputStream.close()
                } catch (ex: java.lang.Exception) {
                }
            }

        }

        return null
    }

    interface Callback {
        fun onPreExecute(downloadItem: DownloadItem)
        fun onSuccess(downloadItem: DownloadItem)
        fun onProgressUpdate(downloadItem: DownloadItem)
        fun onError(throwable: Throwable)
    }
}
