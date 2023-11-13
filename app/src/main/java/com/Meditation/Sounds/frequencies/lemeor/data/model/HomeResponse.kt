package com.Meditation.Sounds.frequencies.lemeor.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.*
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*


@Entity(tableName = "home")
data class HomeResponse(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @TypeConverters(TierConverter::class) var tiers: List<Tier>,
    @TypeConverters(CategoryConverter::class) var categories: List<Category>,
    @TypeConverters(TagConverter::class) var tags: List<Tag>,
    @TypeConverters(AlbumsConverter::class) var albums: List<Album>,
    @TypeConverters(Converters::class) var album: Album,
    @TypeConverters(ProgramConverter::class) var programs: List<Program>,
    @TypeConverters(PlaylistConverter::class) var playlists: List<Playlist>
) {

    @Ignore
    @TypeConverters(IntConverter::class)
    val unlocked_tiers: ArrayList<Int>? = null

    @Ignore
    @TypeConverters(IntConverter::class)
    val unlocked_categories: ArrayList<Int>? = null

    @Ignore
    @TypeConverters(IntConverter::class)
    val unlocked_albums: ArrayList<Int>? = null

}

@Entity(tableName = "tag")
data class Tag(
    @PrimaryKey var id: Int, var name: String, var order: Int, var updated_at: Long
)

@Entity(tableName = "tier")
data class Tier(
    @PrimaryKey var id: Int,
    var name: String,
    var order: Int,
    var updated_at: Long,
    var isShow: Boolean,
    var isPurchased: Boolean
)

@Entity(tableName = "category")
data class Category(
    @PrimaryKey var id: Int,
    var tier_id: Int,
    var name: String,
    var order: Int,
    var updated_at: Long,
    var isShow: Boolean,
    var isPurchased: Boolean
)

@Entity(tableName = "album")
@Parcelize
data class Album(
    @PrimaryKey @SerializedName("_id") var index: Int,
    var id: Int,
    var category_id: Int,
    var tier_id: Int,
    var name: String,
    var image: String,
    var audio_folder: String,
    var is_free: Int,
    var order: Int,
    var order_by: Int,
    var updated_at: Long,
    @TypeConverters(StringConverter::class) var descriptions: List<String>?,
    @TypeConverters(TrackConverter::class) var tracks: List<Track>,
    @TypeConverters(IntConverter::class) var tag: ArrayList<Int>?,
    var isDownloaded: Boolean,
    var isUnlocked: Boolean
) : Parcelable

@Entity(tableName = "track")
@Parcelize
data class Track(
    @PrimaryKey var id: Int,
    var name: String,
    var filename: String,
    var tier_id: Int,
    var category_id: Int,
    var updated_at: Long,
    var isSelected: Boolean,
    var isFavorite: Boolean,
    var isDownloaded: Boolean,
    var albumId: Int,
    var isUnlocked: Boolean,
    var duration: Long,
    @TypeConverters(Converters::class) var album: @RawValue Album?,
    var progress: Int
) : Parcelable {
    constructor(
        id: Int,
        name: String,
        filename: String,
        tier_id: Int,
        category_id: Int,
        updated_at: Long,
        isFavorite: Boolean,
        isDownloaded: Boolean,
        albumId: Int,
        isUnlocked: Boolean,
        duration: Long
    ) : this(
        id,
        name,
        filename,
        tier_id,
        category_id,
        updated_at,
        false,
        isFavorite,
        isDownloaded,
        albumId,
        isUnlocked,
        duration,
        null,
        0
    )
}

@Entity(tableName = "program")
data class Program(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var name: String = "",
    var user_id: Int = 0,
    var order: Int = 0,
    var updated_at: Long = Date().time,
    @TypeConverters(DoubleConverter::class) var records: ArrayList<Double> = arrayListOf(),
    @SerializedName("favorited")
    var isMy: Boolean = true,
    var isUnlocked: Boolean = true,
    @Ignore
    var server_id: Int = 0,
    @Ignore
    var is_dirty: Boolean = false,
    @Ignore
    var is_deleted: Boolean = false,
) {
    constructor(
        id: Int,
        name: String,
        user_id : Int,
        order: Int,
        updated_at: Long,
        records: ArrayList<Double>,
        isMy: Boolean,
        server_id: Int,
        is_dirty: Boolean,
        is_deleted: Boolean,
    ) : this(
        id = id,
        name = name,
        user_id = user_id,
        order = order,
        updated_at = updated_at,
        records = records,
        isMy = isMy,
        isUnlocked = false,
        server_id = server_id,
        is_dirty = is_dirty,
        is_deleted = is_deleted,
    )
}

@Entity(tableName = "playlist")
data class Playlist(
    @PrimaryKey var id: Int,
    var name: String,
    var youtube_id: String,
    var order: Int,
    var updated_at: Long,
    var isSelected: Boolean
) {
    constructor(id: Int, name: String, youtube_id: String, order: Int, updated_at: Long) : this(
        id,
        name,
        youtube_id,
        order,
        updated_at,
        false
    )
}

@Parcelize
@Entity(tableName = "rife")
data class Rife(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var user_id: Int = 0,
    var title: String = "",
    var description: String = "",
    var image: String? = "",
    var audio_folder: String? = "",
    var likes: Int = 0,
    var frequencies: String? = "",
    var subtype: String? = "",
    var type: String? = "",
    var CDate: String? = "",
    var mDate: String? = "",
    @Ignore var tag: String = "",
) : Parcelable {
    fun getFrequency() = if (frequencies?.isEmpty() == true || frequencies == "") arrayListOf()
    else frequencies!!.split('/')
}


