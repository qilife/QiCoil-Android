package com.Meditation.Sounds.frequencies.tasks

import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.fragment.app.FragmentActivity
import android.util.Log
import com.Meditation.Sounds.frequencies.FileEncyptUtil
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.db.QFDatabase
import com.Meditation.Sounds.frequencies.feature.playlist.detail.PlaylistDetailViewModel
import com.Meditation.Sounds.frequencies.models.*
import com.Meditation.Sounds.frequencies.utils.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class SyncMusicTask(var context: Context, listener: ApiListener<Any>, var onUpdateProgressListener: IOnUpdateProgressListener) : BaseTask<Any>(context, listener) {
    private val database = QFDatabase.getDatabase(context.applicationContext)
    val CACHE_FOLDER = File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_FOLDER)
    val CACHE_FOLDER_ADVANCED = File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_ADVANCED_FOLDER)
    val CACHE_FOLDER_ABUNDANCE = File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_ABUNDANCE_FOLDER)
    val CACHE_FOLDER_HIGHER_QUANTUM = File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_HIGHER_QUANTUM_FOLDER)
    var mCountEncypted: Int = 0

    init {
        mCountEncypted = 0
    }

    override fun onProgressUpdate(vararg values: java.lang.Exception?) {
        super.onProgressUpdate(*values)
        if (onUpdateProgressListener != null) {
            onUpdateProgressListener.onProgress(mCountEncypted)
        }
    }

    @Throws(Exception::class)
    override fun callApiMethod(): Boolean {
        val oldAlbums = ArrayList(database.albumDAO().getAll())

        if (!CACHE_FOLDER.exists()) {
            CACHE_FOLDER.mkdir()
        }
        val dataFolders = CACHE_FOLDER.listFiles()

        if (!CACHE_FOLDER_ADVANCED.exists()) {
            CACHE_FOLDER_ADVANCED.mkdir()
        }
        val dataFoldersAdvanced = CACHE_FOLDER_ADVANCED.listFiles()

        if (!CACHE_FOLDER_ABUNDANCE.exists()) {
            CACHE_FOLDER_ABUNDANCE.mkdir()
        }
        val dataFoldersHigherAbundance = CACHE_FOLDER_ABUNDANCE.listFiles()

        if (!CACHE_FOLDER_HIGHER_QUANTUM.exists()) {
            CACHE_FOLDER_HIGHER_QUANTUM.mkdir()
        }
        val dataFoldersHigherQuantum = CACHE_FOLDER_HIGHER_QUANTUM.listFiles()

        val tokenOutput = mApi.token
        mApi.setCredentials("Bearer ".plus(tokenOutput.token))
        val albumPrioritysOutput = mApi.albumPrioritys.albumPrioritys
        val albumAdvancedPrioritysOutput = mApi.albumAdvancedPrioritys.albumPrioritys
        val albumHigherAbundancePriority = mApi.albumsHigherAbundancePriority.albumPrioritys
        val albumHigherQuantumPriority = mApi.albumsHigherQuantumPriority.albumPrioritys
        val newAlbums = mApi.albums.albums//ArrayList<Album>()//
        val newAlbumAdvanced = mApi.albumsAdvanced.albums//ArrayList<Album>()//
        val newAlbumAbundance = mApi.albumsHigherAbundance.albums
        val newAlbumHigherQuantum = mApi.albumsHigherQuantum.albums

        if (dataFolders!!.isNotEmpty() || dataFoldersAdvanced!!.isNotEmpty()
                || dataFoldersHigherAbundance!!.isNotEmpty() || dataFoldersHigherQuantum!!.isNotEmpty()) {

            val listFolders = ArrayList<File>()
            listFolders.add(CACHE_FOLDER)
            listFolders.add(CACHE_FOLDER_ADVANCED)
            listFolders.add(CACHE_FOLDER_ABUNDANCE)
            listFolders.add(CACHE_FOLDER_HIGHER_QUANTUM)

            for (folders in listFolders) {
                for (albumFolder in folders.listFiles()) {
                    if (albumFolder.isDirectory) {
                        //if (folders == CACHE_FOLDER_ADVANCED) CACHE_FOLDER_ADVANCED.path else CACHE_FOLDER.path
                        val albumInfoFile = File(when (folders) {
                            CACHE_FOLDER_ADVANCED -> CACHE_FOLDER_ADVANCED.path
                            CACHE_FOLDER_ABUNDANCE -> CACHE_FOLDER_ABUNDANCE.path
                            CACHE_FOLDER_HIGHER_QUANTUM -> CACHE_FOLDER_HIGHER_QUANTUM.path
                            else -> CACHE_FOLDER.path
                        }, albumFolder.name + "/" + Constants.ALBUM_INFOR_FILE_NAME)
                        var albumInforSongs = arrayListOf<Song>()
                        var addedNewSong = false
                        if (albumInfoFile.exists()) {
                            val abumInforJson = FileEncyptUtil.getJsonFromFile(context, albumInfoFile)
                            if (abumInforJson != null && abumInforJson.length > 0) {
                                albumInforSongs = Gson().fromJson<java.util.ArrayList<Song>>(abumInforJson, object : TypeToken<List<Song>>() {

                                }.type)
                            }
                        }

                        var album: Album? = getAlbum(albumFolder.name, oldAlbums, when (folders) {
                            CACHE_FOLDER_ADVANCED -> 1
                            CACHE_FOLDER_ABUNDANCE -> 2
                            CACHE_FOLDER_HIGHER_QUANTUM -> 3
                            else -> 0
                        })//if (folders == CACHE_FOLDER_ADVANCED) 1 else 0
                        if (album == null) {
                            album = Album()
                            album.name = albumFolder.name
                            album.albumArt = File(albumFolder, Constants.ALBUM_ART_FILE_NAME).path
                            album.downloaded = true
                            when (folders) {
                                CACHE_FOLDER -> {
                                    var isExisted = false
                                    for (i in 0..albumPrioritysOutput.size - 1) {
                                        if (album.name.equals(albumPrioritysOutput.get(i))) {
                                            album.album_priority = i
                                            album.mediaType = Utils.getMediaTypeBasic(i)
                                            album.album_type = 0
                                            isExisted = true
                                            break
                                        }
                                    }
                                    if (!isExisted) {
                                        for (item in newAlbums) {
                                            if (item.name.contains(album.name)) {
                                                isExisted = true
                                                break
                                            }
                                        }
                                    }
                                    if (!isExisted) {
                                        //Delete album if it's removed from server
                                        FilesUtils.deleteRecursive(albumFolder)
//                                        continue
                                    }
                                }
                                CACHE_FOLDER_ADVANCED -> {
                                    var isExisted = false
                                    for (i in 0..albumAdvancedPrioritysOutput.size - 1) {
                                        if (album.name.equals(albumAdvancedPrioritysOutput[i])) {
                                            album.album_priority = i
                                            album.mediaType = Constants.MEDIA_TYPE_ADVANCED
                                            album.album_type = 1
                                            isExisted = true
                                            break
                                        }
                                    }
                                    if (!isExisted) {
                                        for (item in newAlbumAdvanced) {
                                            if (item.name.contains(album.name)) {
                                                isExisted = true
                                                break
                                            }
                                        }
                                    }
                                    if (!isExisted) {
                                        //Delete album if it's removed from server
                                        FilesUtils.deleteRecursive(albumFolder)
//                                        continue
                                    }
                                }
                                CACHE_FOLDER_ABUNDANCE -> {
                                    var isExisted = false
                                    for (i in 0..albumHigherAbundancePriority.size - 1) {
                                        if (album.name.equals(albumHigherAbundancePriority[i])) {
                                            album.album_priority = i
                                            album.mediaType = Constants.MEDIA_TYPE_ABUNDANCE
                                            album.album_type = 2
                                            isExisted = true
                                            break
                                        }
                                    }
                                    if (!isExisted) {
                                        for (item in newAlbumAbundance) {
                                            if (item.name.contains(album.name)) {
                                                isExisted = true
                                                break
                                            }
                                        }
                                    }
                                    if (!isExisted) {
                                        //Delete album if it's removed from server
                                        FilesUtils.deleteRecursive(albumFolder)
//                                        continue
                                    }
                                }
                                CACHE_FOLDER_HIGHER_QUANTUM -> {
                                    var isExisted = false
                                    for (i in 0..albumHigherQuantumPriority.size - 1) {
                                        if (album.name.equals(albumHigherQuantumPriority[i])) {
                                            album.album_priority = i
                                            album.mediaType = Constants.MEDIA_TYPE_HIGHER_QUANTUM
                                            album.album_type = 3
                                            isExisted = true
                                            break
                                        }
                                    }
                                    if (!isExisted) {
                                        for (item in newAlbumHigherQuantum) {
                                            if (item.name.contains(album.name)) {
                                                isExisted = true
                                                break
                                            }
                                        }
                                    }
                                    if (!isExisted) {
                                        //Delete album if it's removed from server
                                        FilesUtils.deleteRecursive(albumFolder)
//                                        continue
                                    }
                                }
                            }

                            album.id = database.albumDAO().insert(album)
                        } else {
                            //Update media type for 3 free album
                            if (folders == CACHE_FOLDER) {
                                for (i in 0..albumPrioritysOutput.size - 1) {
                                    if (album.name.equals(albumPrioritysOutput.get(i))) {
                                        val mediaType = Utils.getMediaTypeBasic(i)
                                        if (album.mediaType != mediaType) {
                                            database.albumDAO().updateMediaTypeById(album.id, mediaType)
                                        }
                                        break
                                    }
                                }
                            }

                            if (album.albumArt == null || (album.albumArt != null && !album.albumArt!!.contains(Constants.ALBUM_ART_FILE_NAME))) {
                                database.albumDAO().updateArtAbumById(album.id, File(albumFolder, Constants.ALBUM_ART_FILE_NAME).path)
                            }
                            if (folders == CACHE_FOLDER) {
                                for (item in newAlbums) {
                                    if (item.name.contains(album.name)) {
                                        oldAlbums.remove(album)
                                    }
                                }
                            }
                            if (folders == CACHE_FOLDER_ADVANCED) {
                                for (item in newAlbumAdvanced) {
                                    if (item.name.contains(album.name)) {
                                        oldAlbums.remove(album)
                                    }
                                }
                            }
                            if (folders == CACHE_FOLDER_ABUNDANCE) {
                                for (item in newAlbumAbundance) {
                                    if (item.name.contains(album.name)) {
                                        oldAlbums.remove(album)
                                    }
                                }
                            }
                            if (folders == CACHE_FOLDER_HIGHER_QUANTUM) {
                                for (item in newAlbumHigherQuantum) {
                                    if (item.name.contains(album.name)) {
                                        oldAlbums.remove(album)
                                    }
                                }
                            }
//                            oldAlbums.remove(album)
                        }
                        val songs = database.songDAO().getByAlbumId(album.id)
                        var numberOfDBSongs = songs.size
                        val songTmps = arrayListOf<Song>()
                        var numberOfMp3Files = 0
                        for (songFile in albumFolder.listFiles()) {
                            try {
                                if (songFile.name != Constants.ALBUM_INFOR_FILE_NAME && !songFile.name.contains(Constants.ALBUM_ART_FILE_NAME, ignoreCase = true) && !songFile.name.startsWith("_tmp")) {
                                    numberOfMp3Files++
                                    var song = getSong(StringsUtils.getFileNameWithoutExtension(songFile.name), songs)
                                    if (song == null) {
                                        song = Song()
                                        song.albumId = album.id
                                        song.albumName = album.name
                                        song.path = songFile.path
                                        song.mediaType = album.mediaType

                                        var encyptPath = songFile?.path
                                        if (StringsUtils.getFileExtension(songFile?.path).equals(Constants.EXTENSION_MP3_FILE, ignoreCase = true)) {
                                            //get songs info
                                            val metaRetriever = MediaMetadataRetriever()
                                            metaRetriever.setDataSource(song.path)
                                            val artist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                                            if(artist != null) {
                                                song.artist = artist
                                            }
                                            song.title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).toString()
                                            song.duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
                                            song.fileName = StringsUtils.getFileNameWithoutExtension(songFile.name)

                                            //No need edit title from new songs
                                            song.editTitleVersion = 1
                                            //No need update file path
                                            song.updateFilePath = 1

                                            if (song.title == "null") {
                                                song.title = StringsUtils.getFileNameWithoutExtension(songFile.name)
                                            }

                                            //Replace album name in song title
                                            song.title = song.title.replace("\n", "").replace(" - David Wong", "")
                                                    .replace(" - David Sereda", "").replace(".mp3", "")
                                                    .replace("Polygonal Quantum Frequencies - ", "")
                                                    .replace("720 Secret Pyramid Tones - Pyramid Scale ", "")
                                                    .replace("The Great Pyramid Hidden Harmonic Frequencies - ", "")
                                                    .replace("The Great Pyramids Hidden Harmonic Frequencies - ", "")
                                                    .replace("The Cube - BinAural Beats+ Multi-Dimensional Holographic Frequency - ", "")
                                                    .replace("The Cube - BinAural Beats+ Multi-Dimensional Holographic Frequency - ", "")

                                            encyptPath = FileEncyptUtil.encryptFile(File(songFile?.path!!), SharedPreferenceHelper.getInstance()[Constants.KEY_DEVICE_ID])
                                            mCountEncypted++
                                            publishProgress(null)
                                            song.path = encyptPath
                                            albumInforSongs.add(song)
                                            addedNewSong = true
                                            numberOfDBSongs++
                                            //Save album infor to file
                                            if (!albumInfoFile.exists()) {
                                                albumInfoFile.createNewFile()
                                            }
                                            FileEncyptUtil.saveJsonToFile(context, albumInfoFile, Gson().toJson(albumInforSongs))

                                            if (File(songFile.path).exists()) {
                                                FileEncyptUtil.deleteFile(songFile.path)
                                            }
                                        } else {
                                            if (albumInforSongs.size > 0) {
                                                for (i in 0..albumInforSongs.size - 1) {
                                                    if (albumInforSongs[i].albumName.replace("\u0027", "'").equals(album.name, ignoreCase = true) &&
                                                            albumInforSongs[i].fileName.replace("\u0027", "'").equals(StringsUtils.getFileNameWithoutExtension(songFile.name), ignoreCase = true)) {
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
                                            continue
                                        }
                                        song.id = database.songDAO().insert(song)
                                        songTmps.add(song)
                                    } else {
                                        //Check and update media type
                                        if (album.mediaType != song.mediaType) {
                                            album.mediaType?.let { database.songDAO().updateMediaTypeById(song.id, it) };
                                        }

                                        songTmps.add(song)
                                        if (song.editTitleVersion == 0) {
                                            if (song.title != null) {
                                                //Replace album name in song title
                                                song.title = song.title.replace("\n", "").replace(" - David Wong", "")
                                                        .replace(" - David Sereda", "").replace(".mp3", "")
                                                song.editTitleVersion = 1
                                                database.songDAO().editTitle(song.id, song.title, song.editTitleVersion!!)
                                            }
                                        }

                                        //update path for encrypt file
                                        if (StringsUtils.getFileExtension(song.path).equals(Constants.EXTENSION_MP3_FILE, ignoreCase = true)) {
                                            var added = false
                                            for (i in 0..albumInforSongs.size - 1) {
                                                if (albumInforSongs[i].albumName.replace("\u0027", "'").equals(album.name, ignoreCase = true) &&
                                                        albumInforSongs[i].fileName.replace("\u0027", "'").equals(StringsUtils.getFileNameWithoutExtension(songFile.name), ignoreCase = true)) {
                                                    added = true
                                                    break
                                                }
                                            }
                                            val oldSongPath = song.path
                                            if (!added) {
                                                song.fileName = StringsUtils.getFileNameWithoutExtension(songFile.name)
                                                val encyptPath = song.path?.let { FileEncyptUtil.encryptFile(File(it), SharedPreferenceHelper.getInstance()[Constants.KEY_DEVICE_ID]) }
                                                encyptPath?.let { database.songDAO().updateEncryptPathFromId(song.id, it) }
                                                mCountEncypted++
                                                publishProgress(null)
                                                song.path = encyptPath
                                                albumInforSongs.add(song)
                                                addedNewSong = true
                                                numberOfDBSongs++
                                                //Save album infor to file
                                                if (!albumInfoFile.exists()) {
                                                    albumInfoFile.createNewFile()
                                                }
                                                FileEncyptUtil.saveJsonToFile(context, albumInfoFile, Gson().toJson(albumInforSongs))
                                            }
                                            if (oldSongPath != null && File(oldSongPath).exists()) {
                                                FileEncyptUtil.deleteFile(oldSongPath)
                                            }
                                        } else {
                                            if (StringsUtils.getFileExtension(songFile.path).equals(Constants.EXTENSION_MP3_FILE, ignoreCase = true)) {
                                                if (song.path != null && File(song.path).exists()) {
                                                    //If encrypted file is exist, then delete mp3 file
                                                    FileEncyptUtil.deleteFile(songFile.path)
                                                } else {
                                                    if (song.updateFilePath == 0) {
                                                        //Encrypt file and update path because file is changed on server
                                                        song.updateFilePath = 1
                                                        var songTitle = song.title
                                                        try {
                                                            val metaRetriever = MediaMetadataRetriever()
                                                            metaRetriever.setDataSource(songFile.path)
                                                            songTitle = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).toString().replace("\n", "").replace(" - David Wong", "")
                                                                    .replace(" - David Sereda", "").replace(".mp3", "")
                                                                    .replace("Polygonal Quantum Frequencies - ", "")
                                                                    .replace("720 Secret Pyramid Tones - Pyramid Scale ", "")
                                                                    .replace("The Great Pyramid Hidden Harmonic Frequencies - ", "")
                                                                    .replace("The Great Pyramids Hidden Harmonic Frequencies - ", "")
                                                                    .replace("The Cube - BinAural Beats+ Multi-Dimensional Holographic Frequency - ", "")
                                                                    .replace("The Cube - BinAural Beats+ Multi-Dimensional Holographic Frequency - ", "")
                                                        } catch (ex: Exception) {
                                                            ex.printStackTrace()
                                                        }
                                                        val encyptPath = songFile.path.let { FileEncyptUtil.encryptFile(File(it), SharedPreferenceHelper.getInstance()[Constants.KEY_DEVICE_ID]) }
                                                        song.path = encyptPath
                                                        database.songDAO().updateFilePath(song.id, song.updateFilePath, encyptPath, songTitle)
                                                        mCountEncypted++
                                                        publishProgress(null)
                                                        albumInforSongs.add(song)
                                                        if (!albumInfoFile.exists()) {
                                                            albumInfoFile.createNewFile()
                                                        }
                                                        FileEncyptUtil.saveJsonToFile(context, albumInfoFile, Gson().toJson(albumInforSongs))
                                                    } else {
                                                        FileEncyptUtil.renameToEncryptFile(songFile.path)
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
                        if (SharedPreferenceHelper.getInstance().getBool(Constants.IS_DOWNLOADED_ALL_ALBUM)) {
                            if (numberOfDBSongs != numberOfMp3Files) {
                                val songs = database.songDAO().getByAlbumId(album.id)
                                for (song in songs) {
                                    var isExisted = false
                                    for (songFile in albumFolder.listFiles()) {
                                        if (File(song.path).exists() && StringsUtils.getFileNameWithoutExtension(File(song.path).name) == StringsUtils.getFileNameWithoutExtension(songFile.name)) {
                                            isExisted = true
                                            break
                                        }
                                    }
                                    if (!isExisted) {
                                        //Remove relating playlist
                                        var mPlaylistItemSongs = database.playlistItemSongDAO().getPlaylistItemSongsBySongIds(song.id)
                                        for (item in mPlaylistItemSongs) {
                                            database.playlistItemSongDAO().delete(item.id)
                                            database.playlistItemDAO().delete(item.playlistItemId)
                                        }
                                        //Remove database
                                        database.songDAO().deleteById(song.id)
                                    }
                                }
                            }
                        } else {
                            //Update art of album
                            database.albumDAO().updateArtAbumById(album.id, File(albumFolder, Constants.ALBUM_ART_FILE_NAME).path)
                        }
                        if (addedNewSong) {
                            if (!albumInfoFile.exists()) {
                                albumInfoFile.createNewFile()
                            }
                            FileEncyptUtil.saveJsonToFile(context, albumInfoFile, Gson().toJson(albumInforSongs))
                            if (albumInfoFile.exists()) {
                                Log.d("MENSSS", albumInfoFile.path)
                            }
                        } else {
                            //If json file is deleted, then should create new
                            if (!albumInfoFile.exists() && songTmps.size > 0) {
                                albumInfoFile.createNewFile()
                                FileEncyptUtil.saveJsonToFile(context, albumInfoFile, Gson().toJson(songTmps))
                                if (albumInfoFile.exists()) {
                                    Log.d("MENSSS", albumInfoFile.path)
                                }
                            }
                        }
                    }
                }
            }

            if (oldAlbums.size > 0) {
                for (album in oldAlbums) {
                    //Delete relating playlist
                    val songs = database.songDAO().getByAlbumId(album.id)
                    for (song in songs) {
                        val mPlaylistItemSongs = database.playlistItemSongDAO().getPlaylistItemSongsBySongIds(song.id)
                        for (item in mPlaylistItemSongs) {
                            database.playlistItemSongDAO().delete(item.id)
                            database.playlistItemDAO().delete(item.playlistItemId)
                        }
                    }

                    database.albumDAO().delete(album.id)
                    database.songDAO().deleteByAlbumId(album.id)
                }
            }

            val localFile = FilesUtils.getFileJsonDefaultLocal()
            if (localFile != null) {
                val playlistJsons = FilesUtils.getPlaylistDefaultQuantum(localFile)
                if (playlistJsons != null && playlistJsons.length > 0) {
                    val playlistArrays = Gson().fromJson<java.util.ArrayList<PlaylistArraysItem>>(playlistJsons, object : TypeToken<List<PlaylistArraysItem>>() {

                    }.type)
                    if (playlistArrays.size > 0) {
                        Collections.reverse(playlistArrays)
                        savePlaylistDefault(playlistArrays)
                    }
                }

                val pathSplit = localFile.split("/")
                if (pathSplit.size > 0) {
                    val fileName = pathSplit.get(pathSplit.size - 1)
                    val newVersion = fileName.replace("QuantumPlaylists_v", "").replace(".json", "")
                    SharedPreferenceHelper.getInstance().set(Constants.PREF_VERSION_DEFAUT_PLAYLIST_JSON, newVersion)
                }

                val file = File(localFile)
                if (file.exists()) {
                    file.delete()
                }
            }

            var versionFromSharePreference = SharedPreferenceHelper.getInstance().get(Constants.PREF_VERSION_DEFAUT_PLAYLIST_JSON)
            if (versionFromSharePreference == null) {
                versionFromSharePreference = ""
            }
            val playlistFromServerPath = SharedPreferenceHelper.getInstance().get(Constants.PREF_DEFAUT_PLAYLIST_JSON)
            if (playlistFromServerPath != null) {
                val pathSplit = playlistFromServerPath.split("/")
                if (pathSplit.size > 0) {
                    val fileName = pathSplit.get(pathSplit.size - 1)
                    val newVersionServer = fileName.replace("QuantumPlaylists_v", "").replace(".json", "")
//                    if (newVersionServer.compareTo(versionFromSharePreference) > 0) {
                    var inputString: String? = null
                    try {
                        // Create a URL for the desired page
                        val url = URL(playlistFromServerPath)
                        // Read all the text returned by the server
                        val buffer = BufferedReader(InputStreamReader(url.openStream()))
                        inputString = buffer.use { it.readText() }
                        buffer.close()
                    } catch (e: IOException) {
                    }
                    if (inputString != null) {
                        SharedPreferenceHelper.getInstance().set(Constants.PREF_DEFAUT_PLAYLIST_CONTENT, inputString)
                        val playlistArrays = Gson().fromJson<java.util.ArrayList<PlaylistArraysItem>>(inputString, object : TypeToken<List<PlaylistArraysItem>>() {

                        }.type)
                        if (playlistArrays.size > 0) {
                            Collections.reverse(playlistArrays)
                            savePlaylistDefault(playlistArrays)

                            SharedPreferenceHelper.getInstance().set(Constants.PREF_VERSION_DEFAUT_PLAYLIST_JSON, newVersionServer)
                        }
                    }
//                    }
                }
            }
        }
//        else {
//            database.albumDAO().clear()
//            database.songDAO().clear()
//        }
        return true
    }

    private fun savePlaylistDefault(playlists: ArrayList<PlaylistArraysItem>) {
        val mViewModel = ViewModelProviders.of(context as FragmentActivity).get(PlaylistDetailViewModel::class.java)
        for (item in playlists) {
            val playlists = database.playlistDAO().getFirstPlaylistByName(item.playlist_name!!)
            var playlistID = -1L
            var totalDuration: Long = 0
            if (playlists.size == 0) {
                val playlist = Playlist(item.playlist_name!!)
                playlistID = database.playlistDAO().insert(playlist)
            } else {
                for (pl in playlists) {
                    if (pl.fromUsers == 0) {
                        playlistID = pl.id
                        totalDuration = pl.totalTime
                        break
                    }
                }
            }

            if (playlistID != -1L) {
                val mPlaylistItems = mViewModel.getPlaylistItems(playlistID).blockingGet()
                var mediaType = Constants.MEDIA_TYPE_BASIC_FREE
                for (songOfAlbum in item.songOfAlbum) {
                    var pathTrack = CACHE_FOLDER.path + "/" + songOfAlbum.album_name + "/" + songOfAlbum.track_name + "." + Constants.EXTENSION_ENCRYPT_FILE
                    var songOfAlbumDB = database.songDAO().getByNameAndAlbumName(songOfAlbum.album_name!!.replace("’", "'"), pathTrack.replace("’", "'"))
                    if (songOfAlbumDB.size == 0) {
                        pathTrack = CACHE_FOLDER.path + "/" + songOfAlbum.album_name + "/" + songOfAlbum.track_name + "." + Constants.EXTENSION_MP3_FILE
                        songOfAlbumDB = database.songDAO().getByNameAndAlbumName(songOfAlbum.album_name!!.replace("’", "'"), pathTrack.replace("’", "'"))
                    }
                    if (songOfAlbumDB.size > 0) {
                        val song = songOfAlbumDB[0]
                        if (mediaType < song.mediaType!!) {
                            mediaType = song.mediaType!!
                        }

                        var isAdded = false
                        for (playlistItem in mPlaylistItems) {
                            if (playlistItem.songs.size > 0 && playlistItem.songs.get(0).item.songId == song.id) {
                                isAdded = true
                                break
                            }
                        }
                        if (!isAdded) {
                            val playlistItemSong = PlaylistItemSong()
                            playlistItemSong.song = song
                            playlistItemSong.songId = song!!.id
                            playlistItemSong.volumeLevel = 1f
                            val fiveMinDefault: Long = 0
                            playlistItemSong.startOffset = fiveMinDefault
                            playlistItemSong.endOffset = song.duration

                            totalDuration += (song.duration - playlistItemSong.startOffset)

                            val playlistItem = PlaylistItem()
                            playlistItem.playlistId = playlistID
                            playlistItem.songs.add(PlaylistItemSongAndSong(playlistItemSong, song))
                            //update database
                            mViewModel.addSongToPlaylist(playlistID, playlistItem)
                        }
                    } else {
                        if (mediaType == Constants.MEDIA_TYPE_BASIC_FREE) {
                            mediaType = Constants.MEDIA_TYPE_BASIC
                        }
                    }
                }

                //update playlist
                database.playlistDAO().updateTotalDuartion(playlistID, totalDuration, mediaType)
            }
        }
        FilesUtils.deletePlaylistDefaultQuantumFile()
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

    interface IOnUpdateProgressListener {
        fun onProgress(countEncypted: Int)
    }
}
