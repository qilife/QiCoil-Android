package com.Meditation.Sounds.frequencies.lemeor.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife

@Dao
interface RifeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rife: Rife?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<Rife>?)

    @Delete
    fun deleteListRife(list: List<Rife>?)

    @Query("SELECT * FROM rife ORDER BY `id` ASC")
    fun getListRife(): LiveData<List<Rife>>

    @Query("SELECT * FROM rife ORDER BY `id` ASC")
    fun getData(): List<Rife>

    @Query("DELETE FROM rife")
    fun clear()
}