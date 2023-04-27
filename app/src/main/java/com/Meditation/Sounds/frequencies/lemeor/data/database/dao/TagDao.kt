package com.Meditation.Sounds.frequencies.lemeor.data.database.dao

import androidx.room.*
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.model.Tag

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tag: Tag?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(listAlbums: List<Tag>?)

    @Delete
    fun deleteTags(list: List<Tag>?)

    @Query("SELECT * FROM tag WHERE name LIKE :searchString ORDER BY `order` ASC")
    fun searchTag(searchString: String): List<Tag>?

    @Query("SELECT * FROM tag ORDER BY `order` ASC")
    fun getData() : List<Tag>?

    @Query("DELETE FROM tag")
    fun clear()
}