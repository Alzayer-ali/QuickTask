package com.example.data

import androidx.room.TypeConverter

enum class RecurrenceType(val label: String, val arabicLabel: String) {
    NONE("None", "بدون تكرار"),
    DAILY("Daily", "يومياً"),
    WEEKDAYS("Weekdays", "أيام العمل"),
    WEEKLY("Weekly", "أسبوعياً"),
    MONTHLY("Monthly", "شهرياً"),
    YEARLY("Yearly", "سنوياً");

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
