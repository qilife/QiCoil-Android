package com.Meditation.Sounds.frequencies.lemeor.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(track: Track?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(listAlbums: List<Track>?)

    @Delete
    fun deleteTracks(list: List<Track>?)

    @Delete
    fun delete(track: Track?)

    @Query("DELETE FROM track")
    fun clear()

    @Query("SELECT * FROM track")
    fun getTracks() : LiveData<List<Track>>

    @Query("SELECT * FROM track WHERE id=:id")
    fun getTrackById(id: Int) : Track?

    @Query("SELECT id FROM track WHERE name=:name")
    fun getTrackIdByName(name: String) : Int

    @Query("UPDATE track SET isDownloaded=:isDownloaded WHERE id=:id")
    fun isTrackDownloaded(isDownloaded: Boolean, id: Int)

    @Query("UPDATE track SET isFavorite=:isFavorite WHERE id=:id")
    fun isTrackFavorite(isFavorite: Boolean, id: Int)

    @Query("SELECT * FROM track WHERE name LIKE :searchString")
    fun searchTrack(searchString: String): List<Track>

    @Query("UPDATE track SET isUnlocked=:isUnlocked WHERE id=:id")
    fun setTrackUnlocked(isUnlocked: Boolean, id: Int)

    @Query("UPDATE track SET isDownloaded=:isDownloaded WHERE id=:id")
    fun setTrackDownloaded(isDownloaded: Boolean, id: Int)

    @Query("UPDATE track SET duration=:d WHERE id=:id")
    fun setDuration(d: Long, id: Int)

    @Query("SELECT * FROM track")
    fun getData() : List<Track>

    @Query("UPDATE track SET isDownloaded=:isDownloaded, isFavorite=:isFavorite, isUnlocked=:isUnlocked WHERE id=:id")
    fun syncTracks(isDownloaded: Boolean, isFavorite: Boolean, isUnlocked: Boolean, id: Int)
}
