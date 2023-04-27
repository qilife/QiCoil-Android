package com.Meditation.Sounds.frequencies.lemeor.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.Meditation.Sounds.frequencies.lemeor.data.model.Playlist
import com.Meditation.Sounds.frequencies.lemeor.data.model.Tag

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(playlist: Playlist?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<Playlist>?)

    @Delete
    fun deletePlaylists(list: List<Playlist>?)

    @Query("SELECT * FROM playlist ORDER BY `order` ASC")
    fun getPlaylists() : LiveData<List<Playlist>>?

    @Query("SELECT * FROM playlist ORDER BY `order` ASC")
    fun getData() : List<Playlist>?

    @Query("DELETE FROM playlist")
    fun clear()
}