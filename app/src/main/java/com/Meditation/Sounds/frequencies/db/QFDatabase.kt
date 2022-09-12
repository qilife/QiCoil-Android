package com.Meditation.Sounds.frequencies.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.Meditation.Sounds.frequencies.db.dao.*
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.Converters
import com.Meditation.Sounds.frequencies.lemeor.data.database.converters.PlaylistItemConverter
import com.Meditation.Sounds.frequencies.models.*

@Database(entities = [Song::class, Album::class, Playlist::class, PlaylistItem::class, PlaylistItemSong::class], version = 8)
@TypeConverters(
    Converters::class,
    PlaylistItemConverter::class
)
abstract class QFDatabase : RoomDatabase() {

    abstract fun songDAO(): SongDAO

    abstract fun albumDAO(): AlbumDAO

    abstract fun playlistDAO(): PlaylistDAO

    abstract fun playlistItemDAO(): PlaylistItemDAO

    abstract fun playlistItemSongDAO(): PlaylistItemSongDAO

    companion object {
        @JvmStatic
        private var INSTANCE: QFDatabase? = null

        private var DB_NAME = "qf_database"

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE play_list "
                        + " ADD COLUMN from_users INTEGER default 0 NOT NULL");
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE songs "
                        + " ADD COLUMN edit_title_version INTEGER default 0 NOT NULL");
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE albums "
                        + " ADD COLUMN album_type INTEGER default 0 NOT NULL");
            }
        }
        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE songs "
                        + " ADD COLUMN file_name TEXT default '' NOT NULL");
            }
        }
        val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE songs "
                        + " ADD COLUMN update_file_path INTEGER default 0 NOT NULL");
            }
        }

        val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE albums "
                        + " ADD COLUMN benefits TEXT default '' NOT NULL")
            }
        }

        val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE songs "
                        + " ADD COLUMN favorite INTEGER default 0 NOT NULL")
            }
        }

        @JvmStatic
        fun getDatabase(context: Context): QFDatabase {
            if (INSTANCE == null) {
                synchronized(QFDatabase::class.java) {
                    if (INSTANCE == null) {
                        // Create database here
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                QFDatabase::class.java, DB_NAME)
                                .allowMainThreadQueries()
                                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
                                        MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                                .build()
                    }
                }
            }
            return INSTANCE!!
        }
    }
}