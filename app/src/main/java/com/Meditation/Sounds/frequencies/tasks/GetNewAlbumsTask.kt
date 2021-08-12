package com.Meditation.Sounds.frequencies.tasks

import android.content.Context
import android.text.TextUtils
import com.Meditation.Sounds.frequencies.FileEncyptUtil
import com.Meditation.Sounds.frequencies.api.ApiListener
import com.Meditation.Sounds.frequencies.db.QFDatabase
import com.Meditation.Sounds.frequencies.models.Album
import com.Meditation.Sounds.frequencies.utils.*
import java.io.File
import java.net.URLDecoder

/**
 * Created by dcmen on 13-Apr-17.
 */
class GetNewAlbumsTask(context: Context, listener: ApiListener<Any>) : BaseTask<Any>(context, listener) {
    private val database = QFDatabase.getDatabase(context.applicationContext)
    val CACHE_FOLDER = File(FileUtils.getSdcardStore(), Constants.DEFAULT_DATA_FOLDER)
    val CACHE_FOLDER_ADVANCED = File(FileUtils.getSdcardStore(), Constants.DEFAULT_DATA_ADVANCED_FOLDER)
    val CACHE_FOLDER_ABUNDANCE = File(FileUtils.getSdcardStore(), Constants.DEFAULT_DATA_ABUNDANCE_FOLDER)
    val CACHE_FOLDER_HIGHER_QUANTUM = File(FileUtils.getSdcardStore(), Constants.DEFAULT_DATA_HIGHER_QUANTUM_FOLDER)

    @Throws(Exception::class)
    override fun callApiMethod(): Any {

        val oldAlbums = ArrayList(database.albumDAO().getAll())
        //Get new data
        val tokenOutput = mApi.token
        mApi.setCredentials("Bearer ".plus(tokenOutput.token))
        val newAlbums = mApi.albums.albums//ArrayList<Album>()//
        val newAlbumAdvanced = mApi.albumsAdvanced.albums//ArrayList<Album>()//
        val newAlbumAbundance = mApi.albumsHigherAbundance.albums
        val newAlbumHigherQuantum = mApi.albumsHigherQuantum.albums
        var albumPrioritys = mApi.albumPrioritys.albumPrioritys
        var albumAdvancedPrioritys = mApi.albumAdvancedPrioritys.albumPrioritys
        var albumHigherAbundancePriority = mApi.albumsHigherAbundancePriority.albumPrioritys
        var albumHigherQuantumPriority = mApi.albumsHigherQuantumPriority.albumPrioritys

        if (albumPrioritys == null) {
            albumPrioritys = arrayListOf()
        }
        if (albumAdvancedPrioritys == null) {
            albumAdvancedPrioritys = arrayListOf()
        }
        if (albumHigherAbundancePriority == null) {
            albumHigherAbundancePriority = arrayListOf()
        }
        if (albumHigherQuantumPriority == null) {
            albumHigherQuantumPriority = arrayListOf()
        }

        var i = 0
        while (i < newAlbums.size) {
            val album = newAlbums[i]
            val songUrls = ArrayList<String>(album.songUrls)
            album.album_type = 0
            album.mediaType = Utils.getMediaTypeBasic(i)
            album.downloaded = isDownloaded(oldAlbums, album, albumPrioritys,
                    false, SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED))
            if (album.downloaded) {
                newAlbums.remove(album)
            } else {
                i++
            }
        }

        var j = 0
        while (j < newAlbumAdvanced.size) {
            val albumAdvanced = newAlbumAdvanced[j]
            val songUrls = ArrayList<String>(albumAdvanced.songUrls)
            albumAdvanced.album_type = 1
            albumAdvanced.mediaType = 3
            albumAdvanced.downloaded = isDownloaded(oldAlbums, albumAdvanced, albumAdvancedPrioritys,
                    true, SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_ADVANCED))
            if (albumAdvanced.downloaded) {
                newAlbumAdvanced.remove(albumAdvanced)
            } else {
                j++
            }
        }
        newAlbums.addAll(newAlbumAdvanced)
        var k = 0
        while (k < newAlbumAbundance.size) {
            val albumAbundance = newAlbumAbundance[k]
            val songUrls = ArrayList<String>(albumAbundance.songUrls)
            albumAbundance.album_type = 2
            albumAbundance.mediaType = 4
            albumAbundance.downloaded = isDownloaded(oldAlbums, albumAbundance, albumHigherAbundancePriority,
                    true, SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_HIGH_ABUNDANCE))
            if (albumAbundance.downloaded) {
                newAlbumAbundance.remove(albumAbundance)
            } else {
                k++
            }
        }
        newAlbums.addAll(newAlbumAbundance)

        var l = 0
        while (l < newAlbumHigherQuantum.size) {
            val albumHigherQuantum = newAlbumHigherQuantum[l]
            val songUrls = ArrayList<String>(albumHigherQuantum.songUrls)
            albumHigherQuantum.album_type = 3
            albumHigherQuantum.mediaType = 5
            albumHigherQuantum.downloaded = isDownloaded(oldAlbums, albumHigherQuantum, albumHigherQuantumPriority,
                    true, SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED_HIGH_QUANTUM))
            if (albumHigherQuantum.downloaded) {
                newAlbumHigherQuantum.remove(albumHigherQuantum)
            } else {
                l++
            }
        }
        newAlbums.addAll(newAlbumHigherQuantum)

        return newAlbums
    }

    private fun getAlbum(name: String, albums: List<Album>, albumType: Int): Album? {
        for (album in albums) {
            if (album.name == name && albumType == album.album_type) {
                return album
            }
        }
        return null
    }

    private fun syncMp3FileInAlbum(album: Album, serverSongs: List<String>) {
        val songs = database.songDAO().getByAlbumId(album.id)
        val serverSongsName = getSongsName(serverSongs)
        for (song in songs) {
            if (!serverSongsName.contains(StringUtils.getFileNameWithoutExtension(StringUtils.getFileName(song.path)))) {
                //Remove relating playlist
                val mPlaylistItemSongs = database.playlistItemSongDAO().getPlaylistItemSongsBySongIds(song.id)
                for (item in mPlaylistItemSongs) {
                    database.playlistItemSongDAO().delete(item.id)
                    database.playlistItemDAO().delete(item.playlistItemId)
                }
                //Remove database
                database.songDAO().deleteById(song.id)
                //Delete file
                if (File(song.path).exists()) {
                    song.path?.let { FileEncyptUtil.deleteFile(it) }
                    if (StringUtils.getFileExtension(song.path).equals(Constants.EXTENSION_MP3_FILE, ignoreCase = true)) {
                        song.path?.let { FileEncyptUtil.deleteFile(it.replace("." + Constants.EXTENSION_MP3_FILE, "." + Constants.EXTENSION_ENCRYPT_FILE)) }
                    } else {
                        song.path?.let { FileEncyptUtil.deleteFile(it.replace("." + Constants.EXTENSION_ENCRYPT_FILE, "." + Constants.EXTENSION_MP3_FILE)) }
                    }
                    if (File(song.path).exists()) {
                        song.path?.let { FileEncyptUtil.deleteFile(it) }
                    }
                }
            }
        }
    }

    private fun getSongsName(serverSongs: List<String>): ArrayList<String> {
        val songsName = ArrayList<String>()
        for (song in serverSongs) {
            songsName.add(StringUtils.getFileNameWithoutExtension(URLDecoder.decode(StringUtils.getFileName(song), Constants.CHARSET)))
        }
        return songsName
    }

    private fun isDownloaded(oldAlbums: ArrayList<Album>, album: Album, albumPrioritys: ArrayList<String>, isAdvanced: Boolean, isPurchased: Boolean): Boolean {
        val albumFolder = File(when {
            album.album_type == 1 -> CACHE_FOLDER_ADVANCED
            album.album_type == 2 -> CACHE_FOLDER_ABUNDANCE
            album.album_type == 3 -> CACHE_FOLDER_HIGHER_QUANTUM
            else -> CACHE_FOLDER
        }, album.name)//if (isAdvanced) CACHE_FOLDER_ADVANCED else CACHE_FOLDER

        if (!TextUtils.isEmpty(album.albumArt)) {
            if (!File(albumFolder, Constants.ALBUM_ART_FILE_NAME).exists()) {
                if ((isAdvanced && !isPurchased) || (!isAdvanced && !isPurchasedAlbum(album.name, albumPrioritys))) {
                    album.songUrls = arrayListOf()
                }
                return false
            }
        }
        if ((!isAdvanced && isPurchasedAlbum(album.name, albumPrioritys)) || (isAdvanced && isPurchased)) {
            //Sync mp3 files
            val albumDb: Album? = getAlbum(album.name, oldAlbums, if (isAdvanced) 1 else 0)
            if (albumDb != null) {
                syncMp3FileInAlbum(albumDb, album.songUrls)
            }

            for (song in album.songUrls) {
                val fileName = URLDecoder.decode(StringUtils.getFileName(song), Constants.CHARSET)
                //File is encoded
                val fileNameWithoutExtension = StringUtils.getFileNameWithoutExtension(fileName)
                val fileEncrypt = fileNameWithoutExtension + "." + Constants.EXTENSION_ENCRYPT_FILE
                if (!File(albumFolder, fileName).exists() && !File(albumFolder, fileEncrypt).exists() && !File(albumFolder, fileNameWithoutExtension).exists()) {
                    return false
                }
            }
        } else {
            album.songUrls = arrayListOf()
        }
        return true
    }

    fun isPurchasedAlbum(albumName: String, albumPrioritys: ArrayList<String>): Boolean {
        var positionPurchase = 3
        if (SharedPreferenceHelper.getInstance().getBool(Constants.KEY_PURCHASED)) {
            positionPurchase = 7
        }
        for (i in 0..albumPrioritys.size - 1) {
            if (i < positionPurchase) {
                if (albumPrioritys.get(i).equals(albumName, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }
}
