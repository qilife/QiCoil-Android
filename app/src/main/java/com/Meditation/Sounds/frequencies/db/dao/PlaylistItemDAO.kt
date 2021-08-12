package com.Meditation.Sounds.frequencies.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.Meditation.Sounds.frequencies.models.PlaylistItem

@Dao
interface PlaylistItemDAO {
    @Query("DELETE FROM playlist_items")
    fun clear()

    @Insert
    fun insert(playlistSong: PlaylistItem):Long

    @Query("DELETE FROM playlist_items WHERE playlist_id in (SELECT id FROM play_list WHERE from_users=0)")
    fun deleteByDefaultPlaylist()

    @Query("DELETE FROM playlist_items WHERE playlist_id=:playlistId")
    fun deleteByPlaylistId(playlistId: Long)

    @Query("DELETE FROM playlist_items WHERE id=:id")
    fun delete(id: Long)

    @Query("SELECT * FROM playlist_items WHERE playlist_id=:playlistId")
    fun getByPlayList(playlistId: Long):List<PlaylistItem>

}