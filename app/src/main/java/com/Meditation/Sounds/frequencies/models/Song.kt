package com.Meditation.Sounds.frequencies.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.Meditation.Sounds.frequencies.utils.Constants
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Admin on 3/22/2017.
 */
@Entity(tableName = "songs"/*,
        indices= [Index( "album_id", unique = false)],
        foreignKeys = [ForeignKey(entity = Album::class, parentColumns = arrayOf("id"), childColumns = arrayOf("album_id"), onDelete = ForeignKey.CASCADE)]*/
)
class Song : Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0

    @ColumnInfo(name = "title")
    var title: String = ""

    @Ignore
    var added: Boolean? = false

    @ColumnInfo(name = "duration")
    var duration: Long = 0

    @ColumnInfo(name = "song_path")
    var path: String? = null

    @ColumnInfo(name = "artist")
    var artist: String = ""

    @ColumnInfo(name = "album_id")
    var albumId: Long = 0

    @ColumnInfo(name = "album_name")
    var albumName: String = ""

    @ColumnInfo(name = "edit_title_version")
    var editTitleVersion: Int = 0

    @ColumnInfo(name = "update_file_path")
    var updateFilePath: Int = 0

    @ColumnInfo(name = "media_type")
    var mediaType: Int? = Constants.MEDIA_TYPE_BASIC

    @ColumnInfo(name = "file_name")
    @SerializedName("file_name")
    var fileName: String = ""

    @ColumnInfo(name = "favorite")
    var favorite: Int = 0

    @Ignore
    constructor(id: Long, title: String) {
        this.id = id
        this.title = title
    }

    @Ignore
    constructor(title: String) {
        this.title = title
    }

    constructor()
}
