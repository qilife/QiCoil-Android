package com.Meditation.Sounds.frequencies.lemeor.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.*
import com.Meditation.Sounds.frequencies.utils.Constants
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
    @PrimaryKey var id: Int = 0,
    var name: String = "",
    var order: Int = 0,
    var updated_at: Long = 0L
)

@Entity(tableName = "tier")
data class Tier(
    @PrimaryKey var id: Int = 0,
    var name: String = "",
    var order: Int = 0,
    var updated_at: Long = 0L,
    var isShow: Boolean = false,
    var isPurchased: Boolean = false
)

@Entity(tableName = "category")
data class Category(
    @PrimaryKey var id: Int = 0,
    var tier_id: Int = 0,
    var name: String = "",
    var order: Int = 0,
    var updated_at: Long = 0L,
    var isShow: Boolean = false,
    var isPurchased: Boolean = false
)

@Entity(tableName = "album", primaryKeys = ["id", "category_id"])
@Parcelize
data class Album(
    var id: Int = 0,
    var category_id: Int = 0,
    var tier_id: Int = 0,
    var name: String = "",
    var image: String = "",
    var audio_folder: String,
    var is_free: Int = 0,
    var order: Int = 0,
    var order_by: Int = 0,
    var updated_at: Long = 0L,
    @TypeConverters(StringConverter::class) var descriptions: List<String>? = null,
    @TypeConverters(TrackConverter::class) var tracks: List<Track> = listOf(),
    @TypeConverters(IntConverter::class) var tag: ArrayList<Int>? = null,
    var isDownloaded: Boolean = false,
    var isUnlocked: Boolean = false,
    var unlock_url: String?,
    var benefits_text: String?,
) : Parcelable

@Entity(tableName = "track")
@Parcelize
data class Track(
    @PrimaryKey var id: Int = 0,
    var name: String = "",
    var filename: String = "",
    var tier_id: Int = 0,
    var category_id: Int = 0,
    var updated_at: Long = 0L,
    var isSelected: Boolean = false,
    var isFavorite: Boolean = false,
    var isDownloaded: Boolean = false,
    var albumId: Int = 0,
    var isUnlocked: Boolean = false,
    var duration: Long = 0L,
    @TypeConverters(Converters::class) var album: @RawValue Album?,
    var progress: Int = 0
) : Parcelable

@Entity(tableName = "program")
data class Program(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var name: String = "",
    var user_id: String = "",
    var order: Int = 0,
    var updated_at: Long = Date().time,
    @TypeConverters(StringArrConverter::class) var records: ArrayList<String> = arrayListOf(),
    var isMy: Boolean = true,
    var isUnlocked: Boolean = true,
    var favorited: Boolean = false,
    var is_dirty: Boolean = false,
    var deleted: Boolean = false,
)

@Entity(tableName = "playlist")
data class Playlist(
    @PrimaryKey var id: Int = 0,
    var name: String = "",
    var youtube_id: String = "",
    var order: Int = 0,
    var updated_at: Long = 0L,
    var isSelected: Boolean = false
)

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
    @Ignore var playtime: Long = 0L,
    @Ignore var tag: String = "",
) : Parcelable {
    fun getFrequency(): List<Float> {
        return if (frequencies?.isEmpty() == true || frequencies == "") arrayListOf()
        else {
            val fs = frequencies!!.split('/')
            fs.filter { it.doubleOrZero() <= Constants.optionsHz[0].second && it.doubleOrZero() >= Constants.optionsHz[0].first }
                .map {
                    it.floatOrZero()
                }
        }
    }
}

fun String.floatOrZero() = try {
    toFloat()
} catch (e: NumberFormatException) {
    0F
}

fun String.doubleOrZero() = try {
    toDouble()
} catch (e: NumberFormatException) {
    0.0
}

data class Search(
    var id: Int,
    var obj: Any,
)

