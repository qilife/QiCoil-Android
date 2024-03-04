package com.Meditation.Sounds.frequencies.lemeor.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.Meditation.Sounds.frequencies.lemeor.data.model.Rife
import com.Meditation.Sounds.frequencies.lemeor.data.model.Track

@Dao
interface RifeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rife: Rife?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Rife>?)

    @Delete
    suspend fun deleteListRife(list: List<Rife>?)

    @Query("SELECT * FROM rife ORDER BY `id` ASC")
    fun getLiveDataRifes(): LiveData<List<Rife>>

    @Query("SELECT * FROM rife ORDER BY `id` ASC")
    suspend fun getData(): List<Rife>

    @Query("SELECT * FROM rife WHERE id=:id")
    suspend fun getRifeById(id: Int) : Rife?

    @Query("DELETE FROM rife")
    suspend fun clear()
}
