package com.Meditation.Sounds.frequencies.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.Meditation.Sounds.frequencies.utils.Constants
import com.google.gson.annotations.SerializedName

/**
 * Created by DC-MEN on 8/18/2018.
 */
@Entity(tableName = "albums")
class Album {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0

    @ColumnInfo(name = "title")
    @SerializedName("album_name")
    var name: String = ""

    @ColumnInfo(name = "album_art")
    @SerializedName("album_image")
    var albumArt: String? = null

    @ColumnInfo(name = "artist")
    @SerializedName("artist")
    var artist: String? = null

    @ColumnInfo(name = "is_downloaded")
    var downloaded: Boolean = false

    @ColumnInfo(name = "album_priority")
    @SerializedName("album_priority")
    var album_priority: Int = 100

    @ColumnInfo(name = "media_type")
    @SerializedName("media_type")
    var mediaType: Int? = Constants.MEDIA_TYPE_BASIC

    //album_type = 0 -> album normal, = 1 -> advance
    @ColumnInfo(name = "album_type")
    @SerializedName("album_type")
    var album_type: Int = 0

    @ColumnInfo(name = "benefits")
    var benefits: String = ""

    @Ignore
    @SerializedName("description")
    var listDescription: ArrayList<String> = ArrayList()

    @Ignore
    var image: Int = -1

    @Ignore
    @SerializedName("track_list")
    var songUrls: ArrayList<String> = ArrayList()

    @Ignore
    var isPurchase: Boolean = false
}
