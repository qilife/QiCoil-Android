package com.Meditation.Sounds.frequencies.models

import androidx.room.*
import com.google.gson.Gson

@Entity(tableName = "playlist_items",
        indices= [Index("playlist_id", unique = false)],
        foreignKeys = [ForeignKey(entity = Playlist::class, parentColumns = arrayOf("id"), childColumns = arrayOf("playlist_id"), onDelete = ForeignKey.CASCADE)]
)
class PlaylistItem {
    constructor(){

    }

    @Ignore
    constructor(playlistId: Long){
        this.playlistId = playlistId
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id:Long = 0

    @ColumnInfo(name = "playlist_id")
    var playlistId:Long  = 0

    @Ignore
    var songs: ArrayList<PlaylistItemSongAndSong> = arrayListOf()

    @Ignore
    var isPlaying: Boolean = false

    @Ignore
    var isPlayingAction: Boolean = false

    companion object {
        @Ignore
        fun clone(source: PlaylistItem): PlaylistItem {
            val stringProject = Gson().toJson(source, PlaylistItem::class.java)
            return Gson().fromJson<PlaylistItem>(stringProject, PlaylistItem::class.java)
        }
    }
}
