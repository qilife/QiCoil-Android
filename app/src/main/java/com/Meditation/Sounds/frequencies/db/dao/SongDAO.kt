package com.Meditation.Sounds.frequencies.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.Meditation.Sounds.frequencies.models.Song

@Dao
interface SongDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(song: Song): Long

    @Query("DELETE FROM songs WHERE id=:songId")
    fun deleteById(songId: Long)

    @Query("DELETE FROM songs WHERE album_id=:albumId")
    fun deleteByAlbumId(albumId: Long)

    @Query("DELETE FROM songs")
    fun clear()

    @Query("SELECT * FROM songs WHERE album_id=:albumId")
    fun getByAlbumId(albumId: Long): List<Song>

    @Query("SELECT * FROM songs WHERE album_id=:albumId ORDER BY title ASC")
    fun getByAlbumIdAsLiveData(albumId: Long): LiveData<List<Song>>

    @Query("SELECT * FROM songs WHERE album_name=:albumName AND song_path=:pathTrack")
    fun getByNameAndAlbumName(albumName: String, pathTrack: String): List<Song>

    @Query("SELECT * FROM songs WHERE title LIKE  :keyword")
    fun searchTracks(keyword: String): List<Song>

    @Query("SELECT * FROM songs WHERE title LIKE :keyword AND album_id=:albumId ")
    fun searchTracksByAlbumId(keyword: String, albumId: Long): List<Song>

    @Query("SELECT * FROM songs WHERE title LIKE :keyword AND album_id IN (:albumIds) ORDER BY title ASC")
    fun searchTracksByAlbum(keyword: String, albumIds : IntArray): List<Song>

    @Query("UPDATE songs SET title=:newTitle, edit_title_version=:isEdit WHERE id=:id")
    fun editTitle(id : Long, newTitle : String, isEdit : Int)

    @Query("UPDATE songs SET update_file_path=:isUpdateFilePath, song_path=:newPath, title=:newTitle WHERE id=:id")
    fun updateFilePath(id : Long, isUpdateFilePath : Int, newPath: String, newTitle: String)

    @Query("UPDATE songs SET song_path=:songPath WHERE id=:songId")
    fun updateEncryptPathFromId(songId:Long = 0, songPath : String)

    @Query("SELECT * FROM songs WHERE update_file_path = 1 LIMIT 1")
    fun getFirstSongUpdatedPath():List<Song>

    @Query("UPDATE songs SET media_type=:mediaType WHERE id=:songId")
    fun updateMediaTypeById(songId:Long = 0, mediaType : Int)

    @Query("UPDATE songs SET duration=:totalDuration WHERE id=:songId")
    fun updateDurationSong(songId: Long, totalDuration: Long)

    @Query("UPDATE songs SET favorite=:favorite WHERE id=:songId AND album_id=:albumId")
    fun updateFavorite(songId: Long,albumId: Long,favorite:Int)

    @Query("UPDATE songs SET favorite=0 WHERE id=:songId AND album_id=:albumId")
    fun removeFavorite(songId: Long,albumId: Long)

    @Query("SELECT * FROM songs WHERE favorite==1")
    fun getSongFavorite() : List<Song>
}
