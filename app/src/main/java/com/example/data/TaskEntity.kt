package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "tasks")
@TypeConverters(PriorityConverter::class)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val dueDateMillis: Long? = null,
    val dueTimeHour: Int? = null,
    val dueTimeMinute: Int? = null,
    val priority: Priority = Priority.MEDIUM,
    val category: String = "General",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val calendarEventId: Long? = null,
    val isCalendarSynced: Boolean = false
)
