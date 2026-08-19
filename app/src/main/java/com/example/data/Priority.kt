package com.example.data

import androidx.room.TypeConverter

enum class Priority(val label: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3);

    companion object {
        fun fromString(value: String?): Priority {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}

class PriorityConverter {
    @TypeConverter
    fun fromPriority(priority: Priority?): String {
        return priority?.name ?: Priority.MEDIUM.name
    }

    @TypeConverter
    fun toPriority(value: String?): Priority {
        return Priority.fromString(value)
    }
}
