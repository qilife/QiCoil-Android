package com.Meditation.Sounds.frequencies.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.Meditation.Sounds.frequencies.models.Playlist

@Dao
interface PlaylistDAO {

    @Insert
    fun insert(playlist: Playlist): Long

    @Update
    fun update(playlist: Playlist)

    @Query("DELETE FROM play_list")
    fun clear()

    @Query("DELETE FROM play_list WHERE id=:playlistId")
    fun delete(playlistId: Long)

    @Query("DELETE FROM play_list WHERE from_users=0")
    fun deleteDefaultPlaylists()

    @Query("SELECT * FROM play_list WHERE id=:playlistId")
    fun getByIdAsLiveData(playlistId: Long): LiveData<Playlist?>

    @Query("SELECT * FROM play_list WHERE id=:playlistId")
    fun getById(playlistId: Long): Playlist?

    @Query("SELECT * FROM play_list WHERE modified_date=(SELECT MAX(modified_date) FROM play_list)")
    fun getLastModifiedPlaylist(): Playlist?

    //@Query("SELECT * FROM play_list WHERE modified_date=(SELECT MIN(modified_date) FROM play_list)")
    @Query("SELECT * FROM play_list WHERE from_users = 1 ORDER BY id ASC LIMIT 1")
    fun getFirstModifiedPlaylist(): Playlist?

    @Query("SELECT * FROM play_list ORDER BY created_date DESC")
    fun getAll(): List<Playlist>

    @Query("SELECT * FROM play_list ORDER BY created_date DESC")
    fun getAllAsLiveData(): LiveData<List<Playlist>>

    @Query("SELECT * FROM play_list WHERE from_users = 1 LIMIT 1")
    fun getFirstPlaylist(): List<Playlist>

    @Query("UPDATE play_list SET total_time=:totalDuration, media_type=:mediaType WHERE id=:playlistId")
    fun updateTotalDuartion(playlistId: Long, totalDuration: Long, mediaType: Int)

    @Query("UPDATE play_list SET total_time=:totalDuration, media_type=:mediaType,from_users=:fromUser WHERE id=:playlistId")
    fun updateTotalDurationPlaylist(playlistId: Long, totalDuration: Long, mediaType: Int, fromUser: Int)

    @Query("UPDATE play_list SET from_users=:fromUser WHERE id=:playlistId")
    fun updateFromUserFromId(playlistId: Long = 0, fromUser: Int)

    @Query("SELECT * FROM play_list WHERE title=:playlistName")
    fun getFirstPlaylistByName(playlistName: String): List<Playlist>

    @Query("SELECT * FROM play_list WHERE title LIKE :keyword AND id IN (:ids) ORDER BY title ASC")
    fun searchPlaylistByName(keyword: String, ids: IntArray): List<Playlist>

    @Query("SELECT * FROM play_list WHERE title LIKE :keyword ORDER BY title ASC")//AND id IN (:ids)
    fun searchPlaylist(keyword: String): List<Playlist>//, ids : IntArray
}
