package com.Meditation.Sounds.frequencies.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.Meditation.Sounds.frequencies.models.Album

@Dao
interface AlbumDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(album: Album): Long

    @Query("DELETE FROM albums")
    fun clear()

    @Query("DELETE FROM albums WHERE id=:id")
    fun delete(id: Long)

    @Query("SELECT * FROM albums ORDER BY title")
    fun getAll(): List<Album>

    @Query("SELECT * FROM albums WHERE media_type<=2 ORDER BY album_priority ASC")
    fun getAllAsLiveData(): LiveData<List<Album>>

    @Query("SELECT * FROM albums WHERE media_type = 3 ORDER BY album_priority ASC")
    fun getAlbumAdvancedAsLiveData(): LiveData<List<Album>>

    @Query("SELECT * FROM albums WHERE media_type = 4 ORDER BY album_priority ASC")
    fun getAlbumsHigherAbundance(): LiveData<List<Album>>

    @Query("SELECT * FROM albums WHERE media_type = 5 ORDER BY album_priority ASC")
    fun getAlbumsHigherQuantum(): LiveData<List<Album>>

    @Query("SELECT COUNT(*) FROM albums")
    fun getCount(): Int

    @Query("SELECT COUNT(*) FROM albums WHERE is_downloaded=:isDownloaded")
    fun getDownloadedCount(isDownloaded: Boolean): Int

    @Query("UPDATE albums SET album_art=:albumArt WHERE id=:albumId")
    fun updateArtAbumById(albumId: Long = 0, albumArt: String)

    @Query("UPDATE albums SET media_type=:mediaType WHERE id=:albumId")
    fun updateMediaTypeById(albumId: Long = 0, mediaType: Int)

    @Query("UPDATE albums SET benefits=:description WHERE title=:albumTitle")
    fun updateDescriptionAlbum(albumTitle: String, description: String)

    @Query("SELECT * FROM albums WHERE title=:albumTitle")
    fun getAlbumByTitle(albumTitle: String): Album

    @Query("SELECT * FROM albums WHERE title LIKE :keyword AND id IN (:albumIds) ORDER BY title ASC")
    fun searchAlbum(keyword: String, albumIds: IntArray): List<Album>
}
