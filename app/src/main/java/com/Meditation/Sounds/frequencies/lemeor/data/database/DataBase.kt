package com.Meditation.Sounds.frequencies.lemeor.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.Meditation.Sounds.frequencies.BuildConfig
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.*
import com.Meditation.Sounds.frequencies.lemeor.data.database.dao.*
import com.Meditation.Sounds.frequencies.lemeor.data.model.*

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
    ], version = 3
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
    Converters::class
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
                database.execSQL("ALTER TABLE program ADD COLUMN is_dirty INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE program ADD COLUMN is_deleted INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE program ADD COLUMN server_id INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE program ADD COLUMN user_id INTEGER DEFAULT 0 NOT NULL")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS rife (" +
                            "`id` INTEGER DEFAULT 0 NOT NULL, " +
                            "`user_id` INTEGER DEFAULT 0 NOT NULL, " +
                            "`title` TEXT DEFAULT '' NOT NULL, " +
                            "`description` TEXT DEFAULT '' NOT NULL, " +
                            "`image` TEXT DEFAULT '', " +
                            "`audio_folder` TEXT DEFAULT '', " +
                            "`likes` INTEGER DEFAULT 0 NOT NULL, " +
                            "`frequencies` TEXT DEFAULT '', " +
                            "`type` TEXT DEFAULT 'rife', " +
                            "`subtype` TEXT DEFAULT '', " +
                            "`CDate` TEXT DEFAULT '', " +
                            "`mDate` TEXT DEFAULT '', " +
                            " PRIMARY KEY(id))"
                )
            }
        }

        @Volatile
        private var instance: DataBase? = null

        fun getInstance(context: Context): DataBase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                DataBase::class.java,
                BuildConfig.DB_NAME
            )
                .allowMainThreadQueries()
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
    }
}