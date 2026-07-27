package com.example.expensetracker.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "time_entries",
    indices = [Index(value = ["dateString"])]
)
@Serializable
data class TimeEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val title: String,
    val description: String? = null,
    val category: String = "General",
    val startTimeMillis: Long,
    val endTimeMillis: Long? = null,
    val durationSeconds: Long = 0L,
    val isRunning: Boolean = false,
    val isBillable: Boolean = false,
    val hourlyRate: Double? = null,
    val associatedExpenseId: Long? = null,
    val dateString: String
)
