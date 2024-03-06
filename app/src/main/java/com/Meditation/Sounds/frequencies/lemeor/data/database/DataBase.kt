package com.Meditation.Sounds.frequencies.lemeor.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.Meditation.Sounds.frequencies.BuildConfig
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.AlbumsConverter
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.CategoryConverter
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.Converters
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.DoubleConverter
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.IntConverter
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.PlaylistConverter
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.PlaylistItemConverter
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.ProgramConverter
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.RifeConverter
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.StringArrConverter
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.StringConverter
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.TagConverter
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.TierConverter
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.TrackConverter
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.AlbumDao
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.CategoryDao
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.HomeDao
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.PlaylistDao
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.ProgramDao
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.RifeDao
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.TagDao
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.TierDao
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.TrackDao
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Category
import com.Meditation.Sounds.frequencies.lemeor.data.model.HomeResponse
import com.Meditation.Sounds.frequencies.lemeor.data.model.Playlist
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.model.Tag
import com.Meditation.Sounds.frequencies.lemeor.data.model.Tier
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track

@Database(
    entities = [
        HomeResponse::class,
        Tier::class,
        Category::class,
        Tag::class,
        Album::class,
        Track::class,
        Program::class,
        Playlist::class,
        Rife::class,
    ], version = 6
)

@TypeConverters(
    TierConverter::class,
    CategoryConverter::class,
    TagConverter::class,
    AlbumsConverter::class,
    PlaylistConverter::class,
    PlaylistItemConverter::class,
    ProgramConverter::class,
    TrackConverter::class,
    IntConverter::class,
    DoubleConverter::class,
    RifeConverter::class,
    StringConverter::class,
    StringArrConverter::class,
    Converters::class,
)
abstract class DataBase : RoomDatabase() {

    abstract fun homeDao(): HomeDao
    abstract fun tierDao(): TierDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao
    abstract fun albumDao(): AlbumDao
    abstract fun trackDao(): TrackDao
    abstract fun programDao(): ProgramDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun rifeDao(): RifeDao

    companion object {

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE home ADD COLUMN tiers TEXT DEFAULT 0 NOT NULL")
                database.execSQL("CREATE TABLE IF NOT EXISTS tier (id INTEGER DEFAULT 0 NOT NULL, name TEXT DEFAULT 0 NOT NULL, `order` INTEGER DEFAULT 0 NOT NULL, updated_at INTEGER DEFAULT 0 NOT NULL, isShow INTEGER DEFAULT 0 NOT NULL, isPurchased INTEGER DEFAULT 0 NOT NULL, PRIMARY KEY(id))")
                database.execSQL("ALTER TABLE category ADD COLUMN tier_id INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE album ADD COLUMN tier_id INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE track ADD COLUMN tier_id INTEGER DEFAULT 0 NOT NULL")
            }
        }
        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE program ADD COLUMN favorited INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE program ADD COLUMN deleted INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE program ADD COLUMN is_dirty INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE program ADD COLUMN user_id TEXT DEFAULT '' NOT NULL")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS rife (" + "`id` INTEGER DEFAULT 0 NOT NULL, " + "`user_id` INTEGER DEFAULT 0 NOT NULL, " + "`title` TEXT DEFAULT '' NOT NULL, " + "`description` TEXT DEFAULT '' NOT NULL, " + "`image` TEXT DEFAULT '', " + "`audio_folder` TEXT DEFAULT '', " + "`likes` INTEGER DEFAULT 0 NOT NULL, " + "`frequencies` TEXT DEFAULT '', " + "`type` TEXT DEFAULT 'rife', " + "`subtype` TEXT DEFAULT '', " + "`CDate` TEXT DEFAULT '', " + "`mDate` TEXT DEFAULT '', " + " PRIMARY KEY(id))"
                )
            }
        }
        private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS album")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `album` (`id` INTEGER NOT NULL, " + "`category_id` INTEGER NOT NULL, " + "`tier_id` INTEGER NOT NULL, " + "`name` TEXT NOT NULL, `image` TEXT NOT NULL, " + "`audio_folder` TEXT NOT NULL, " + "`is_free` INTEGER NOT NULL, " + "`order` INTEGER NOT NULL, " + "`order_by` INTEGER NOT NULL, " + "`updated_at` INTEGER NOT NULL, " + "`descriptions` TEXT, " + "`tracks` TEXT NOT NULL, " + "`tag` TEXT, `isDownloaded` INTEGER NOT NULL, " + "`isUnlocked` INTEGER NOT NULL, " + "PRIMARY KEY(`id`, `category_id`))"
                )
            }
        }

        private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE album ADD COLUMN benefits_text TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE album ADD COLUMN unlock_url TEXT DEFAULT NULL")
            }
        }
        private val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val cursor = database.query("SELECT * FROM program")
                val idColumnIndex = cursor.getColumnIndex("id")
                val recordsColumnIndex = cursor.getColumnIndex("records")
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(idColumnIndex)
                    val records = cursor.getString(recordsColumnIndex)
                    val recordsList = records.split(",").map { it.trim() }
                    val recordsString = recordsList.joinToString(",")
                    val contentValues = ContentValues()
                    contentValues.put("records", recordsString)
                    database.update(
                        "program",
                        SQLiteDatabase.CONFLICT_NONE,
                        contentValues,
                        "id=:id",
                        arrayOf(id)
                    )
                }
            }
        }

        @Volatile
        private var instance: DataBase? = null

        fun getInstance(context: Context): DataBase = instance ?: synchronized(this) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, DataBase::class.java, BuildConfig.DB_NAME
        ).addMigrations(
            MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6
        ).build()
    }
}
