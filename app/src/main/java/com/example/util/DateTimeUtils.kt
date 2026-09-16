package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    private val sameYearFormat = ThreadLocal.withInitial {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    }
    private val diffYearFormat = ThreadLocal.withInitial {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    }

    @Volatile
    private var lastCacheCheckMillis = 0L
    private var cachedTodayStartMillis = 0L
    private var cachedTomorrowStartMillis = 0L
    private var cachedYesterdayStartMillis = 0L
    private var cachedCurrentYear = 0

    // High-performance LRU cache for formatted due date strings (capacity 256)
    private val formattedDateCache = object : LinkedHashMap<String, String>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
            return size > 256
        }
    }

    private fun ensureDayBoundsCache(now: Long = System.currentTimeMillis()) {
        if (cachedTodayStartMillis == 0L || now < cachedTodayStartMillis || now >= cachedTomorrowStartMillis || now - lastCacheCheckMillis > 60_000L) {
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cachedTodayStartMillis = cal.timeInMillis
            cachedCurrentYear = cal.get(Calendar.YEAR)

            cal.add(Calendar.DAY_OF_YEAR, 1)
            cachedTomorrowStartMillis = cal.timeInMillis

            cal.add(Calendar.DAY_OF_YEAR, -2)
            cachedYesterdayStartMillis = cal.timeInMillis

            lastCacheCheckMillis = now
            synchronized(formattedDateCache) {
                formattedDateCache.clear()
            }
        }
    }

    /**
     * Formats the due date and time range in 24-hour format (e.g. "Today • 15:00 → 22:00" or "Tomorrow • 14:30").
     */
    fun formatDueDate(
        dueDateMillis: Long?,
        startHour: Int?,
        startMinute: Int?,
        endHour: Int? = null,
        endMinute: Int? = null
    ): String {
        if (dueDateMillis == null) return "No due date"

        val cacheKey = "$dueDateMillis-$startHour-$startMinute-$endHour-$endMinute"
        synchronized(formattedDateCache) {
            formattedDateCache[cacheKey]?.let { return it }
        }

        val now = System.currentTimeMillis()
        ensureDayBoundsCache(now)

        val dateStr = when {
            isDueToday(dueDateMillis) -> "Today"
            isTomorrow(dueDateMillis) -> "Tomorrow"
            isYesterday(dueDateMillis) -> "Yesterday"
            else -> {
                val targetCal = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
                if (targetCal.get(Calendar.YEAR) == cachedCurrentYear) {
                    sameYearFormat.get()?.format(Date(dueDateMillis)) ?: ""
                } else {
                    diffYearFormat.get()?.format(Date(dueDateMillis)) ?: ""
                }
            }
        }

        val result = if (startHour != null && startMinute != null) {
            val startTimeStr = format24Hour(startHour, startMinute)
            if (endHour != null && endMinute != null) {
                val endTimeStr = format24Hour(endHour, endMinute)
                "$dateStr • $startTimeStr → $endTimeStr"
            } else {
                "$dateStr • $startTimeStr"
            }
        } else {
            dateStr
        }

        synchronized(formattedDateCache) {
            formattedDateCache[cacheKey] = result
        }

        return result
    }

    /**
     * Formats hour and minute into standard 24-hour string (HH:mm) with zero regex overhead.
     */
    fun format24Hour(hour: Int, minute: Int): String {
        val h = if (hour < 10) "0$hour" else hour.toString()
        val m = if (minute < 10) "0$minute" else minute.toString()
        return "$h:$m"
    }

    fun isOverdue(dueDateMillis: Long?, hour: Int?, minute: Int?): Boolean {
        if (dueDateMillis == null) return false

        val now = System.currentTimeMillis()
        ensureDayBoundsCache(now)

        // If the due date is strictly before today, it is definitely overdue
        if (dueDateMillis < cachedTodayStartMillis) return true
        // If the due date is tomorrow or later, it is definitely not overdue
        if (dueDateMillis >= cachedTomorrowStartMillis) return false

        // Due today: check specific time if provided
        return if (hour != null && minute != null) {
            val dueTimeMillis = cachedTodayStartMillis + (hour * 3600_000L) + (minute * 60_000L)
            dueTimeMillis < now
        } else {
            false
        }
    }

    fun isDueToday(dueDateMillis: Long?): Boolean {
        if (dueDateMillis == null) return false
        val now = System.currentTimeMillis()
        ensureDayBoundsCache(now)
        return dueDateMillis >= cachedTodayStartMillis && dueDateMillis < cachedTomorrowStartMillis
    }

    fun isToday(dueDateMillis: Long): Boolean = isDueToday(dueDateMillis)

    fun isTomorrow(dueDateMillis: Long): Boolean {
        val now = System.currentTimeMillis()
        ensureDayBoundsCache(now)
        val dayAfterTomorrow = cachedTomorrowStartMillis + 86_400_000L
        return dueDateMillis >= cachedTomorrowStartMillis && dueDateMillis < dayAfterTomorrow
    }

    fun isYesterday(dueDateMillis: Long): Boolean {
        val now = System.currentTimeMillis()
        ensureDayBoundsCache(now)
        return dueDateMillis >= cachedYesterdayStartMillis && dueDateMillis < cachedTodayStartMillis
    }

    fun isUpcoming(dueDateMillis: Long?): Boolean {
        if (dueDateMillis == null) return false
        val now = System.currentTimeMillis()
        ensureDayBoundsCache(now)
        return dueDateMillis >= cachedTodayStartMillis
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_WEEK_IN_MONTH) == cal2.get(Calendar.DAY_OF_WEEK_IN_MONTH) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun getTodayStartMillis(): Long {
        ensureDayBoundsCache()
        return cachedTodayStartMillis
    }

    fun getTomorrowStartMillis(): Long {
        ensureDayBoundsCache()
        return cachedTomorrowStartMillis
    }

    fun getStartOfDay(timeMillis: Long): Long {
        ensureDayBoundsCache()
        if (timeMillis >= cachedTodayStartMillis && timeMillis < cachedTomorrowStartMillis) {
            return cachedTodayStartMillis
        }
        if (timeMillis >= cachedTomorrowStartMillis && timeMillis < cachedTomorrowStartMillis + 86_400_000L) {
            return cachedTomorrowStartMillis
        }
        if (timeMillis >= cachedYesterdayStartMillis && timeMillis < cachedTodayStartMillis) {
            return cachedYesterdayStartMillis
        }

        return Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Calculates the next occurrence date in millis for recurring tasks.
     */
    fun calculateNextRecurrenceDate(baseDateMillis: Long?, recurrence: com.example.data.RecurrenceType): Long {
        val cal = Calendar.getInstance().apply {
            if (baseDateMillis != null) {
                timeInMillis = baseDateMillis
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the task was scheduled in the past, align base to today before adding step
        if (cal.timeInMillis < todayCal.timeInMillis) {
            cal.timeInMillis = todayCal.timeInMillis
        }

        when (recurrence) {
            com.example.data.RecurrenceType.DAILY -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            com.example.data.RecurrenceType.WEEKDAYS -> {
                do {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                } while (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)
            }
            com.example.data.RecurrenceType.WEEKLY -> {
                cal.add(Calendar.DAY_OF_YEAR, 7)
            }
            com.example.data.RecurrenceType.MONTHLY -> {
                cal.add(Calendar.MONTH, 1)
            }
            com.example.data.RecurrenceType.YEARLY -> {
                cal.add(Calendar.YEAR, 1)
            }
            com.example.data.RecurrenceType.NONE -> {}
        }

        return cal.timeInMillis
    }
}
