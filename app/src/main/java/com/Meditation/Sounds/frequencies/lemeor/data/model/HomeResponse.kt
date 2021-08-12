package com.Meditation.Sounds.frequencies.lemeor.data.model

import android.os.Parcelable
import androidx.room.*
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.*
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Entity(tableName = "home")
data class HomeResponse(
        @PrimaryKey(autoGenerate = true) val id: Int,
        @TypeConverters(TierConverter::class) val tiers: List<Tier>,
        @TypeConverters(CategoryConverter::class) val categories: List<Category>,
        @TypeConverters(TagConverter::class) val tags: List<Tag>,
        @TypeConverters(AlbumConverter::class) val albums: List<Album>,
        @TypeConverters(ProgramConverter::class) val programs: List<Program>,
        @TypeConverters(PlaylistConverter::class) val playlists: List<Playlist>
)

@Entity(tableName = "tag")
data class Tag(
        @PrimaryKey val id: Int,
        val name: String,
        val order: Int,
        val updated_at: Long
)

@Entity(tableName = "tier")
data class Tier(
        @PrimaryKey val id: Int,
        val name: String,
        val order: Int,
        val updated_at: Long,
        var isShow: Boolean,
        var isPurchased: Boolean
)

@Entity(tableName = "category")
data class Category(
        @PrimaryKey val id: Int,
        val tier_id: Int,
        val name: String,
        val order: Int,
        val updated_at: Long,
        var isShow: Boolean,
        var isPurchased: Boolean
)

@Entity(tableName = "album")
@Parcelize
data class Album(
        @PrimaryKey val id: Int,
        val category_id: Int,
        var tier_id: Int,
        val name: String,
        val image: String,
        val audio_folder: String,
        val is_free: Int,
        val order: Int,
        val updated_at: Long,
        @TypeConverters(StringConverter::class) val descriptions: List<String>?,
        @TypeConverters(TrackConverter::class) val tracks: List<Track>,
        @TypeConverters(IntConverter::class) val tag: ArrayList<Int>?,
        var isDownloaded: Boolean,
        var isUnlocked: Boolean
) : Parcelable

@Entity(tableName = "track")
@Parcelize
data class Track(
        @PrimaryKey val id: Int,
        val name: String,
        val filename: String,
        var tier_id: Int,
        val updated_at: Long,
        @Ignore var isSelected: Boolean,
        var isFavorite: Boolean,
        var isDownloaded: Boolean,
        var albumId: Int,
        var isUnlocked: Boolean,
        var duration: Long,
        @Ignore var album: @RawValue Album?,
        @Ignore var progress: Int
) : Parcelable {
    constructor(
            id: Int,
            name: String,
            filename: String,
            tier_id: Int,
            updated_at: Long,
            isFavorite: Boolean,
            isDownloaded: Boolean,
            albumId: Int,
            isUnlocked: Boolean,
            duration: Long
    ) : this(id, name, filename, tier_id, updated_at, false, isFavorite, isDownloaded, albumId, isUnlocked, duration, null, 0)
}

@Entity(tableName = "program")
data class Program(
        @PrimaryKey(autoGenerate = true) val id: Int,
        val name: String,
        val order: Int,
        val updated_at: Long,
        @TypeConverters(IntConverter::class) var records: ArrayList<Int>,
        val isMy: Boolean,
        @Ignore var isUnlocked: Boolean
) {
    constructor(id: Int, name: String, order: Int, updated_at: Long, records: ArrayList<Int>, isMy: Boolean)
            : this(id, name, order, updated_at, records, isMy, false)
}

@Entity(tableName = "playlist")
data class Playlist(
        @PrimaryKey val id: Int,
        val name: String,
        val youtube_id: String,
        val order: Int,
        val updated_at: Long,
        @Ignore var isSelected: Boolean
) {
    constructor(id: Int, name: String, youtube_id: String, order: Int, updated_at: Long)
            : this(id, name, youtube_id, order, updated_at, false)
}

