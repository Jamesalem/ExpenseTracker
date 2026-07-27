package com.example.expensetracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.data.model.Subscription
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions WHERE isActive = 1 ORDER BY nextDueDateString ASC")
    fun observeActiveSubscriptions(): Flow<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getById(id: Long): Subscription?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: Subscription): Long

    @Update
    suspend fun update(subscription: Subscription)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT SUM(amount) FROM subscriptions WHERE isActive = 1")
    fun observeTotalMonthlySubscriptionsCost(): Flow<Double?>

    @Query("SELECT * FROM subscriptions WHERE isActive = 1 AND nextDueDateString = :date")
    suspend fun getSubscriptionsDueOn(date: String): List<Subscription>
}
