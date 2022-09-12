package com.Meditation.Sounds.frequencies.models

import androidx.room.*
import com.Meditation.Sounds.frequencies.utils.Constants
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "play_list")
class Playlist {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0

    @ColumnInfo(name = "title")
    var title: String = ""

    @ColumnInfo(name = "media_type")
    var mediaType: Int? = Constants.MEDIA_TYPE_BASIC_FREE

    @ColumnInfo(name = "total_time")
    var totalTime: Long = 0

    @ColumnInfo(name = "created_date")
    var dateCreated: Long = Date().time

    @ColumnInfo(name = "modified_date")
    var dateModified: Long = Date().time

    @ColumnInfo(name = "from_users")
    var fromUsers: Int = 0

    @ColumnInfo(name = "songs")
    var songs:ArrayList<PlaylistItemSong> = arrayListOf()

    constructor()

    @Ignore
    constructor(title: String) {
        this.title = title
        dateCreated = Date().time
        dateModified = dateCreated
    }

}
