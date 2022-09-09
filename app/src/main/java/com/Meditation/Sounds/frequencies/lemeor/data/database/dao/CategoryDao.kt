package com.Meditation.Sounds.frequencies.lemeor.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.Meditation.Sounds.frequencies.lemeor.data.model.Category

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(category: Category?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<Category>?)

    @Query("UPDATE category SET isShow=:isShow WHERE id=:id")
    fun updateShowStatus(isShow: Boolean, id: Int)

    @Query("UPDATE category SET isPurchased=:isPurchased WHERE id=:id")
    fun updatePurchaseStatus(isPurchased: Boolean, id: Int)

    @Delete
    fun delete(category: Category)

    @Delete
    fun deleteCategories(list: List<Category>?)

    @Query("SELECT * FROM category WHERE isShow=:isShow ORDER BY `order` ASC")
    fun getCategories(isShow: Boolean) : LiveData<List<Category>>?

    @Query("SELECT * FROM category WHERE tier_id=:tierId ORDER BY `order` ASC")
    fun getCategoriesByTierId(tierId: Int) : LiveData<List<Category>>?

    @Query("SELECT name FROM category WHERE id=:categoryId ORDER BY `order` ASC")
    fun getCategoryNameById(categoryId: Int) : String?

    @Query("SELECT * FROM category ORDER BY `order` ASC")
    fun getData() : List<Category>?
}