package com.example.data

import androidx.room.TypeConverter

enum class RecurrenceType(val label: String) {
    NONE("None"),
    DAILY("Daily"),
    WEEKDAYS("Weekdays"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly");

    companion object {
        fun fromString(value: String?): RecurrenceType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: NONE
        }
    }
}

class RecurrenceTypeConverter {
    @TypeConverter
    fun fromRecurrence(recurrence: RecurrenceType?): String {
        return recurrence?.name ?: RecurrenceType.NONE.name
    }

    @TypeConverter
    fun toRecurrence(value: String?): RecurrenceType {
        return RecurrenceType.fromString(value)
    }
}
