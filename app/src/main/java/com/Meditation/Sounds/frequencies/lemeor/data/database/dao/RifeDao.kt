package com.Meditation.Sounds.frequencies.lemeor.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife

@Dao
interface RifeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rife: Rife?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Rife>?)

    @Delete
    suspend fun deleteListRife(list: List<Rife>?)

    @Query("SELECT * FROM rife ORDER BY `id` ASC")
    fun getListRife(): LiveData<List<Rife>>

    @Query("SELECT * FROM rife ORDER BY `id` ASC")
    suspend fun getData(): List<Rife>

    @Query("DELETE FROM rife")
    suspend fun clear()
}