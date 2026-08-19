package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    fun formatDueDate(dueDateMillis: Long?, hour: Int?, minute: Int?): String {
        if (dueDateMillis == null) return "No due date"

        val targetCal = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
        val nowCal = Calendar.getInstance()

        val isToday = isSameDay(targetCal, nowCal)
        nowCal.add(Calendar.DAY_OF_YEAR, 1)
        val isTomorrow = isSameDay(targetCal, nowCal)
        nowCal.add(Calendar.DAY_OF_YEAR, -2)
        val isYesterday = isSameDay(targetCal, nowCal)

        val dateStr = when {
            isToday -> "Today"
            isTomorrow -> "Tomorrow"
            isYesterday -> "Yesterday"
            targetCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) -> {
                SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(dueDateMillis))
            }
            else -> {
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(dueDateMillis))
            }
        }

        return if (hour != null && minute != null) {
            val timeCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(timeCal.time)
            "$dateStr • $timeStr"
        } else {
            dateStr
        }
    }

    fun isOverdue(dueDateMillis: Long?, hour: Int?, minute: Int?): Boolean {
        if (dueDateMillis == null) return false

        val targetCal = Calendar.getInstance().apply {
            timeInMillis = dueDateMillis
            if (hour != null && minute != null) {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            } else {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
        }

        return targetCal.timeInMillis < System.currentTimeMillis()
    }

    fun isDueToday(dueDateMillis: Long?): Boolean {
        if (dueDateMillis == null) return false
        val targetCal = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
        val nowCal = Calendar.getInstance()
        return isSameDay(targetCal, nowCal)
    }

    fun isToday(dueDateMillis: Long): Boolean = isDueToday(dueDateMillis)

    fun isTomorrow(dueDateMillis: Long): Boolean {
        val targetCal = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
        val tomCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        return isSameDay(targetCal, tomCal)
    }

    fun isUpcoming(dueDateMillis: Long?): Boolean {
        if (dueDateMillis == null) return false
        val targetCal = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
        val nowCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return targetCal.timeInMillis >= nowCal.timeInMillis
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun getTodayStartMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun getTomorrowStartMillis(): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
