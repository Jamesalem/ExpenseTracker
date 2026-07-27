package com.example.expensetracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.data.model.TimeEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeEntryDao {

    @Query("SELECT * FROM time_entries ORDER BY startTimeMillis DESC")
    fun observeAll(): Flow<List<TimeEntry>>

    @Query("SELECT * FROM time_entries WHERE isRunning = 1 LIMIT 1")
    fun observeRunningEntry(): Flow<TimeEntry?>

    @Query("SELECT * FROM time_entries WHERE isRunning = 1 LIMIT 1")
    suspend fun getRunningEntry(): TimeEntry?

    @Query("SELECT * FROM time_entries WHERE dateString = :dateString ORDER BY startTimeMillis DESC")
    fun observeByDate(dateString: String): Flow<List<TimeEntry>>

    @Query("SELECT * FROM time_entries WHERE id = :id")
    suspend fun getById(id: Long): TimeEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TimeEntry): Long

    @Update
    suspend fun update(entry: TimeEntry)

    @Query("DELETE FROM time_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT SUM(durationSeconds) FROM time_entries WHERE dateString = :dateString")
    fun observeTotalSecondsForDate(dateString: String): Flow<Long?>
}
