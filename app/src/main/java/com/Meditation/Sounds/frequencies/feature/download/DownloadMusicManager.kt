package com.Meditation.Sounds.frequencies.feature.download

import android.app.Dialog
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.widget.ImageView
import android.widget.TextView
import com.Meditation.Sounds.frequencies.FileEncyptUtil
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.api.TaskApi
import com.Meditation.Sounds.frequencies.db.QFDatabase
import com.Meditation.Sounds.frequencies.feature.base.BaseActivity
import com.Meditation.Sounds.frequencies.models.Album
import com.Meditation.Sounds.frequencies.models.Song
import com.Meditation.Sounds.frequencies.tasks.BaseTask
import com.Meditation.Sounds.frequencies.utils.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.net.URLDecoder
import java.util.concurrent.Executors

class DownloadMusicManager(private var activity: BaseActivity, private var albums: ArrayList<Album>, private var callback: Callback?) : AsyncTask<Void, Void, String>()
        , DownloadFileTask.Callback, ApiListener<Any> {
    private val database = QFDatabase.getDatabase(activity.applicationContext)
    protected var mApi: TaskApi
    private val executor = Executors.newFixedThreadPool(3)
    private var downloadedFile = 0
    private var totalDownloadFile = 0
    val CACHE_FOLDER = File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_FOLDER)
    val CACHE_FOLDER_ADVANCED = File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_ADVANCED_FOLDER)
    val CACHE_FOLDER_ABUNDANCE = File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_ABUNDANCE_FOLDER)
    val CACHE_FOLDER_HIGHER_QUANTUM = File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_HIGHER_QUANTUM_FOLDER)
    var albumPrioritysOutput = ArrayList<String>()
    var albumAdvancedPrioritysOutput = ArrayList<String>()
    var albumHigherAbundancePriority = ArrayList<String>()
    var albumHigherQuantumPriority = ArrayList<String>()
    var hmAlbumInfors: HashMap<String, ArrayList<Song>>
    var oldAlbums = ArrayList<Album>()
    var isDownloadError: Boolean = false
    private var mDownloadDialog: Dialog
    private var mDialogRc: RecyclerView? = null
    private var mAdapter: DownloadAdapter? = null
    private var mDialogTitle: TextView? = null
    private var mDialogClose: ImageView? = null

    init {
        oldAlbums = ArrayList(database.albumDAO().getAll())
        hmAlbumInfors = HashMap<String, ArrayList<Song>>()
        mApi = TaskApi(activity)
        if (SharedPreferenceHelper.getInstance().get(Constants.PREF_SESSION_ID) != null && SharedPreferenceHelper.getInstance().get(Constants.PREF_SESSION_ID).length > 0) {
            mApi.setCredentials(SharedPreferenceHelper.getInstance().get(Constants.PREF_SESSION_ID))
        }
        if (!CACHE_FOLDER.exists()) {
            CACHE_FOLDER.mkdir()
        }
        if (!CACHE_FOLDER_ADVANCED.exists()) {
            CACHE_FOLDER_ADVANCED.mkdir()
        }
        if (!CACHE_FOLDER_ABUNDANCE.exists()) {
            CACHE_FOLDER_ABUNDANCE.mkdir()
        }
        if (!CACHE_FOLDER_HIGHER_QUANTUM.exists()) {
            CACHE_FOLDER_HIGHER_QUANTUM.mkdir()
        }

        mDownloadDialog = Dialog(activity)
        mDownloadDialog.setCancelable(false)
        mDownloadDialog.setContentView(R.layout.dialog_download_music)
        mDialogRc = mDownloadDialog.findViewById(R.id.list)
        mDialogClose = mDownloadDialog.findViewById(R.id.imvClose)
        mDialogTitle = mDownloadDialog.findViewById(R.id.description)
        mDialogRc?.layoutManager = LinearLayoutManager(activity)
        mAdapter = DownloadAdapter(ArrayList())
        mDialogRc?.adapter = mAdapter

        mDialogClose?.setOnClickListener {
            mDownloadDialog.dismiss()
        }
    }

    fun showDialog() {
        if (mDownloadDialog != null) {
            if (mAdapter!!.data.size > 0) {
                mDownloadDialog.show()
            }
        }
    }

    fun dismissDialog() {
        if (mDownloadDialog.isShowing) {
            mDownloadDialog.dismiss()
        }
    }

    override fun doInBackground(vararg p0: Void?): String? {
        val tokenOutput = mApi.token
        mApi.setCredentials("Bearer ".plus(tokenOutput.token))
        albumPrioritysOutput = mApi.albumPrioritys.albumPrioritys
        albumAdvancedPrioritysOutput = mApi.albumAdvancedPrioritys.albumPrioritys
        albumHigherAbundancePriority = mApi.albumsHigherAbundancePriority.albumPrioritys
        albumHigherQuantumPriority = mApi.albumsHigherQuantumPriority.albumPrioritys

        for (album in albums) {
            addToDownload(album, true)
        }
        activity.runOnUiThread {
            mDialogTitle?.text = activity.getText(R.string.txt_downloading).toString()
                    .plus(" (").plus(downloadedFile).plus("/").plus(totalDownloadFile).plus(")")
        }
        return null
    }

    fun additionDownloadData(additionAlbums: ArrayList<Album>) {
        for (item in additionAlbums) {
            var added = false
            for (album in albums) {
                if (item.name.equals(album.name, ignoreCase = true) && album.songUrls != null && album.songUrls.size > 0) {
                    added = true
                    break
                }
            }
            if (!added) {
                addToDownload(item, false)
            }
        }
    }

    fun addToDownload(album: Album, isDownloadArts: Boolean) {
        val albumFolder = File(when {
            album.album_type == 1 -> CACHE_FOLDER_ADVANCED
            album.album_type == 2 -> CACHE_FOLDER_ABUNDANCE
            album.album_type == 3 -> CACHE_FOLDER_HIGHER_QUANTUM
            else -> CACHE_FOLDER
        }, album.name)
        if (!albumFolder.exists()) {
            albumFolder.mkdir()
        }
        if (!TextUtils.isEmpty(album.albumArt)) {
            val file = File(albumFolder, Constants.ALBUM_ART_FILE_NAME)
            //File is encoded
            val fileWithoutExtension = File(albumFolder, StringsUtils.getFileNameWithoutExtension(file.name))
            if (!file.exists() && !fileWithoutExtension.exists() && isDownloadArts) {
                DownloadFileTask(album, album.albumArt!!, 0, false, this@DownloadMusicManager).executeOnExecutor(executor)
                totalDownloadFile++
            }
        }
        for (song in album.songUrls) {
            val fileName = URLDecoder.decode(StringsUtils.getFileName(song), Constants.CHARSET)
            //File is encoded
            val fileNameWithoutExtension = StringsUtils.getFileNameWithoutExtension(fileName)
            val fileEncrypt = fileNameWithoutExtension + "." + Constants.EXTENSION_ENCRYPT_FILE
            if (!File(albumFolder, fileName).exists() && !File(albumFolder, fileEncrypt).exists() && !File(albumFolder, fileNameWithoutExtension).exists()) {
                DownloadFileTask(album, song, album.songUrls.indexOf(song) + 1, true, this@DownloadMusicManager).executeOnExecutor(executor)
                totalDownloadFile++
            }
        }
        this@DownloadMusicManager.callback?.onPercentRatio("0/" + totalDownloadFile, 0, totalDownloadFile)
    }

    override fun onSuccess(downloadItem: DownloadItem) {
        val index = mAdapter!!.data.indexOf(downloadItem)
        if (index >= 0) {
            mAdapter!!.data.remove(downloadItem)
            mAdapter!!.notifyItemRemoved(index)
        }
        downloadedFile++
        mDialogTitle?.text = activity.getText(R.string.txt_downloading).toString()
                .plus(" (").plus(downloadedFile).plus("/").plus(totalDownloadFile).plus(")")

        this@DownloadMusicManager.callback?.onPercentRatio("" + downloadedFile + "/" + totalDownloadFile, downloadedFile, totalDownloadFile)
        if (downloadedFile == totalDownloadFile) {
            SharedPreferenceHelper.getInstance().setBool(Constants.IS_DOWNLOADED_ALL_ALBUM, true)
            this@DownloadMusicManager.callback?.onSuccess()
        }
        //Need encript file and save to database
        var albumInforSongs = ArrayList<Song>()

//        val albumFolder = if (downloadItem.albumType == 1) CACHE_FOLDER_ADVANCED else CACHE_FOLDER
        val albumFolder = when {
            downloadItem.albumType == 1 -> CACHE_FOLDER_ADVANCED
            downloadItem.albumType == 2 -> CACHE_FOLDER_ABUNDANCE
            downloadItem.albumType == 3 -> CACHE_FOLDER_HIGHER_QUANTUM
            else -> CACHE_FOLDER
        }
        if (!hmAlbumInfors.containsKey(downloadItem.albumName)) {
            val albumInforFile = File(albumFolder, downloadItem.albumName + "/" + Constants.ALBUM_INFOR_FILE_NAME)
            if (albumInforFile.exists()) {
                val abumInforJson = FileEncyptUtil.getJsonFromFile(activity, albumInforFile)
                if (abumInforJson != null && abumInforJson.isNotEmpty()) {
                    try{
                        albumInforSongs = Gson().fromJson<java.util.ArrayList<Song>>(abumInforJson, object : TypeToken<List<Song>>() {

                        }.type)
                    }catch (e: IllegalStateException){
                    }
                }
            }
            hmAlbumInfors.put(downloadItem.albumName, albumInforSongs)
        } else {
            albumInforSongs = hmAlbumInfors.get(downloadItem.albumName)!!
        }
        var album: Album? = getAlbum(downloadItem.albumName, oldAlbums, downloadItem.albumType)
        //Insert album if don't exist
        if (album == null) {
            album = Album()
            album.name = downloadItem.albumName
            album.albumArt = File(albumFolder, downloadItem.albumName + "/" + Constants.ALBUM_ART_FILE_NAME).path
            album.downloaded = true

            when (downloadItem.albumType) {
                0 -> {
                    for (i in 0..albumPrioritysOutput.size - 1) {
                        if (album.name.equals(albumPrioritysOutput.get(i))) {
                            album.album_priority = i
                            album.mediaType = Utils.getMediaTypeBasic(i)
                            album.album_type = 0
                            break
                        }
                    }
                }
                1 -> {
                    //Advance albums
                    for (i in 0..albumAdvancedPrioritysOutput.size - 1) {
                        if (album.name.equals(albumAdvancedPrioritysOutput[i])) {
                            album.album_priority = i
                            album.mediaType = Constants.MEDIA_TYPE_ADVANCED
                            album.album_type = 1
                            break
                        }
                    }
                }
                2 -> {
                    //ABUNDANCE albums
                    for (i in 0..albumHigherAbundancePriority.size - 1) {
                        if (album.name.equals(albumHigherAbundancePriority[i])) {
                            album.album_priority = i
                            album.mediaType = Constants.MEDIA_TYPE_ABUNDANCE
                            album.album_type = 2
                            break
                        }
                    }
                }
                3 -> {
                    //QUANTUM  albums
                    for (i in 0..albumHigherQuantumPriority.size - 1) {
                        if (album.name.equals(albumHigherQuantumPriority[i])) {
                            album.album_priority = i
                            album.mediaType = Constants.MEDIA_TYPE_HIGHER_QUANTUM
                            album.album_type = 3
                            break
                        }
                    }
                }
            }

            album.id = database.albumDAO().insert(album)
            oldAlbums.add(album)
        }
        val albumMp3File = File(albumFolder, downloadItem.albumName + "/" + URLDecoder.decode(StringsUtils.getFileName(downloadItem.url), Constants.CHARSET))
        if (albumMp3File.exists()) {
            syncSongs(album, albumMp3File.path, albumInforSongs)
        }
    }

    private fun syncSongs(album: Album, songUrl: String, albumInforSongs: ArrayList<Song>) {
        var songFileName = StringsUtils.getFileName(songUrl)
        val songs = database.songDAO().getByAlbumId(album.id)
        try {
            if (songFileName != Constants.ALBUM_INFOR_FILE_NAME && !songFileName.contains(Constants.ALBUM_ART_FILE_NAME, ignoreCase = true) && !songFileName.startsWith("_tmp")) {
                var song = getSong(StringsUtils.getFileNameWithoutExtension(songFileName), songs)
                if (song == null) {
                    song = Song()
                    song.albumId = album.id
                    song.albumName = album.name
                    song.path = songUrl
                    song.mediaType = album.mediaType

                    var encyptPath = songUrl
                    if (StringsUtils.getFileExtension(songUrl).equals(Constants.EXTENSION_MP3_FILE, ignoreCase = true)) {
                        //get songs info
                        val metaRetriever = MediaMetadataRetriever()
                        metaRetriever.setDataSource(song.path)
                        song.artist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)!!
                        song.title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).toString()
                        song.duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
                        song.fileName = StringsUtils.getFileNameWithoutExtension(songFileName)

                        //No need edit title from new songs
                        song.editTitleVersion = 1
                        //No need update file path
                        song.updateFilePath = 1

                        if (song.title == "null") {
                            song.title = StringsUtils.getFileNameWithoutExtension(songFileName)
                        }

                        encyptPath = FileEncyptUtil.encryptFile(File(songUrl), SharedPreferenceHelper.getInstance()[Constants.KEY_DEVICE_ID])
                        song.path = encyptPath

                        saveSongToJsonAlbumFile(album.name, album.album_type, song)

                        if (File(songUrl).exists()) {
                            FileEncyptUtil.deleteFile(songUrl)
                        }
                    } else {
                        if (albumInforSongs.size > 0) {
                            for (i in 0..albumInforSongs.size - 1) {
                                if (albumInforSongs[i].albumName.replace("\u0027", "'").equals(album.name, ignoreCase = true) &&
                                        albumInforSongs[i].fileName.replace("\u0027", "'").equals(StringsUtils.getFileNameWithoutExtension(songFileName), ignoreCase = true)) {
                                    song.artist = albumInforSongs[i].artist
                                    song.title = albumInforSongs[i].title
                                    song.duration = albumInforSongs[i].duration
                                    break
                                }
                            }
                        }
                        song.path = encyptPath
                    }
                    if (song.title == null || (song.title != null && song.title.isEmpty())) {
                        return
                    }
                    song.id = database.songDAO().insert(song)
                } else {
                    //Check and update media type
                    if (album.mediaType != song.mediaType) {
                        album.mediaType?.let { database.songDAO().updateMediaTypeById(song.id, it) };
                    }

                    //update path for encrypt file
                    if (StringsUtils.getFileExtension(song.path).equals(Constants.EXTENSION_MP3_FILE, ignoreCase = true)) {
                        var added = false
                        for (i in 0..albumInforSongs.size - 1) {
                            if (albumInforSongs[i].albumName.replace("\u0027", "'").equals(album.name, ignoreCase = true) &&
                                    albumInforSongs[i].fileName.replace("\u0027", "'").equals(StringsUtils.getFileNameWithoutExtension(songFileName), ignoreCase = true)) {
                                added = true
                                break
                            }
                        }
                        val oldSongPath = song.path
                        if (!added) {
                            song.fileName = StringsUtils.getFileNameWithoutExtension(songFileName)
                            val encyptPath = song.path?.let { FileEncyptUtil.encryptFile(File(it), SharedPreferenceHelper.getInstance()[Constants.KEY_DEVICE_ID]) }
                            encyptPath?.let { database.songDAO().updateEncryptPathFromId(song.id, it) }
                            song.path = encyptPath

                            saveSongToJsonAlbumFile(album.name, album.album_type, song)
                        }
                        if (oldSongPath != null && File(oldSongPath).exists()) {
                            FileEncyptUtil.deleteFile(oldSongPath)
                        }
                    } else {
                        if (StringsUtils.getFileExtension(songUrl).equals(Constants.EXTENSION_MP3_FILE, ignoreCase = true)) {
                            if (song.path != null && File(song.path).exists()) {
                                //If encrypted file is exist, then delete mp3 file
                                FileEncyptUtil.deleteFile(songUrl)
                            } else {
                                if (song.updateFilePath == 0) {
                                    //Encrypt file and update path because file is changed on server
                                    song.updateFilePath = 1
                                    var songTitle = song.title
                                    val encyptPath = songUrl.let { FileEncyptUtil.encryptFile(File(it), SharedPreferenceHelper.getInstance()[Constants.KEY_DEVICE_ID]) }
                                    song.path = encyptPath
                                    database.songDAO().updateFilePath(song.id, song.updateFilePath, encyptPath, songTitle)
                                    saveSongToJsonAlbumFile(album.name, album.album_type, song)
                                } else {
                                    FileEncyptUtil.renameToEncryptFile(songUrl)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveSongToJsonAlbumFile(albumName: String, albumType: Int, song: Song) {
        var albumInforSongs = ArrayList<Song>()

//        val albumFolder = if (albumType == 1) CACHE_FOLDER_ADVANCED else CACHE_FOLDER
        val albumFolder = when (albumType) {
            1 -> CACHE_FOLDER_ADVANCED
            2 -> CACHE_FOLDER_ABUNDANCE
            3 -> CACHE_FOLDER_HIGHER_QUANTUM
            else -> CACHE_FOLDER
        }
        val albumInforFile = File(albumFolder, albumName + "/" + Constants.ALBUM_INFOR_FILE_NAME)
        if (albumInforFile.exists()) {
            val abumInforJson = FileEncyptUtil.getJsonFromFile(activity, albumInforFile)
            if (abumInforJson != null && abumInforJson.length > 0) {
                albumInforSongs = Gson().fromJson<java.util.ArrayList<Song>>(abumInforJson, object : TypeToken<List<Song>>() {

                }.type)
            }
        }
        albumInforSongs.add(song)
        if (!albumInforFile.exists()) {
            albumInforFile.createNewFile()
        }
        FileEncyptUtil.saveJsonToFile(activity, albumInforFile, Gson().toJson(albumInforSongs))
    }

    private fun getAlbum(name: String, albums: List<Album>, albumType: Int): Album? {
        for (album in albums) {
            if (album.name == name && albumType == album.album_type) {
                return album
            }
        }
        return null
    }

    private fun getSong(name: String, songs: List<Song>): Song? {
        for (song in songs) {
            if (StringsUtils.getFileNameWithoutExtension(File(song.path).name) == name) {
                return song
            }
        }
        return null
    }

    override fun onError(throwable: Throwable) {
        executor.shutdownNow()
        if (mDownloadDialog.isShowing) {
            try {
                mDownloadDialog.dismiss()
            } catch (e: IllegalArgumentException) {
            }
        }
        if (!isDownloadError) {
            isDownloadError = true
            this@DownloadMusicManager.callback?.onError(throwable)
        }
    }

    override fun onProgressUpdate(downloadItem: DownloadItem) {
        val index = mAdapter!!.data.indexOf(downloadItem)
        if (index >= 0) {
//            mAdapter!!.notifyItemChanged(index)
        }
        mAdapter!!.notifyDataSetChanged()
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

    interface Callback {
        fun onSuccess()
        fun onPercentRatio(ratio: String, currentStep: Int, totalStep: Int)
        fun onError(throwable: Throwable)
    }
}
