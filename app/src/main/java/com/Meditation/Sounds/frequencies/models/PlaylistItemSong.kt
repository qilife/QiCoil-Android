package com.Meditation.Sounds.frequencies.models

import androidx.room.*
import java.io.Serializable

@Entity(tableName = "playlist_item_songs",
        indices= [Index(value = ["playlist_item_id"], unique = false)],
        foreignKeys = [ForeignKey(entity = PlaylistItem::class, parentColumns = arrayOf("id"), childColumns = arrayOf("playlist_item_id"), onDelete = ForeignKey.CASCADE)]
)
class PlaylistItemSong: Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "playlist_songs_id")
    var id:Long = 0

    @ColumnInfo(name = "playlist_item_id")
    var playlistItemId:Long  = 0

    @ColumnInfo(name = "song_id")
    var songId: Long = 0

    @ColumnInfo(name = "volume_level")
    var volumeLevel: Float  = 1f

    @ColumnInfo(name = "start_offset")
    var startOffset:Long = 0

    @ColumnInfo(name = "end_offset")
    var endOffset:Long = 0

    @Ignore
    var song:Song? = null
}
