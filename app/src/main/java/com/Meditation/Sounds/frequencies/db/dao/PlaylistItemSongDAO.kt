package com.Meditation.Sounds.frequencies.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.Meditation.Sounds.frequencies.models.PlaylistItemSong
import com.Meditation.Sounds.frequencies.models.PlaylistItemSongAndSong

@Dao
interface PlaylistItemSongDAO {
    @Query("DELETE FROM playlist_item_songs")
    fun clear()

    @Insert
    fun insert(playlist: PlaylistItemSong):Long

    @Query("DELETE FROM playlist_item_songs WHERE playlist_item_id=:playlistItemId")
    fun deleteByPlaylistItemId(playlistItemId: Long)

    @Query("SELECT * FROM playlist_item_songs WHERE song_id=:songId")
    fun getPlaylistItemSongsBySongIds(songId: Long): List<PlaylistItemSong>

    @Query("DELETE FROM playlist_item_songs WHERE playlist_songs_id=:id")
    fun delete(id: Long)

    @Query("SELECT playlist_item_songs.*, songs.* FROM playlist_item_songs INNER JOIN songs ON playlist_item_songs.song_id=songs.id WHERE playlist_item_id=:playlistItemId")
    fun getByPlaylistItemId(playlistItemId: Long): List<PlaylistItemSongAndSong>

    @Query("UPDATE playlist_item_songs SET volume_level=:volumeLevel WHERE playlist_songs_id = :playlistSongId")
    fun updateVolumeLevel(playlistSongId : Long, volumeLevel: Float)

    @Query("UPDATE playlist_item_songs SET start_offset=:startOffset WHERE playlist_songs_id = :playlistSongId")
    fun updateStartOffset(playlistSongId : Long, startOffset: Long)

    @Query("UPDATE playlist_item_songs SET end_offset=:endOffset WHERE playlist_songs_id = :playlistSongId")
    fun updateEndOffset(playlistSongId : Long, endOffset: Long)

    @Query("UPDATE playlist_item_songs SET start_offset=:startOffset, end_offset=:endOffset WHERE playlist_songs_id = :playlistSongId")
    fun updateStartEndOffset(playlistSongId : Long, startOffset: Long, endOffset: Long)

//    @Query("SELECT * FROM playlist_items INNER JOIN playlist_songs ON playlist_items.id = playlist_songs.playlist_item_id  WHERE playlist_id=:playlistId ")
//    fun getByPlayList(playlistId: Long):List<PlaylistItemSong>

}