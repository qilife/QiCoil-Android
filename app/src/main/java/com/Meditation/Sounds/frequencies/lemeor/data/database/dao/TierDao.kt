package com.Meditation.Sounds.frequencies.lemeor.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.Meditation.Sounds.frequencies.lemeor.data.model.Category

import com.Meditation.Sounds.frequencies.lemeor.data.model.Tier

@Dao
interface TierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tier: Tier?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<Tier>?)

    @Query("UPDATE tier SET isShow=:isShow WHERE id=:id")
    fun updateShowStatus(isShow: Boolean, id: Int)

    @Query("SELECT tier.isShow FROM tier WHERE id=:tierId")
    fun getShowStatus(tierId: Int) : Boolean

    @Query("UPDATE tier SET isPurchased=:isPurchased WHERE id=:id")
    fun updatePurchaseStatus(isPurchased: Boolean, id: Int)

    @Delete
    fun delete(tier: Tier)

    @Delete
    fun deleteTiers(list: List<Tier>?)

    @Query("SELECT * FROM tier ORDER BY `order` ASC")
    fun getTiers() : LiveData<List<Tier>>?

    @Query("SELECT * FROM tier WHERE isShow=:isShow ORDER BY `order` ASC")
    fun getTiers(isShow: Boolean) : LiveData<List<Tier>>?

    @Query("SELECT * FROM tier ORDER BY `order` ASC")
    fun getData() : List<Tier>?

    @Query("SELECT name FROM tier WHERE id=:tierId ORDER BY `order` ASC")
    fun getTierNameById(tierId: Int) : String?
}