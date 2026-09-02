package com.example.util

import com.example.data.RecurrenceType
import java.util.Calendar

data class ParsedTaskResult(
    val cleanTitle: String,
    val dueDateMillis: Long? = null,
    val startHour: Int? = null,
    val startMinute: Int? = null,
    val endHour: Int? = null,
    val endMinute: Int? = null,
    val recurrence: RecurrenceType = RecurrenceType.NONE
)

object SmartTimeParser {

    /**
     * Converts Eastern Arabic numerals (٠-٩) and Persian numerals to standard ASCII digits (0-9).
     */
    fun normalizeArabicDigits(input: String): String {
        val arabicChars = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val persianChars = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        var result = input
        for (i in 0..9) {
            result = result.replace(arabicChars[i], (i + '0'.code).toChar())
            result = result.replace(persianChars[i], (i + '0'.code).toChar())
        }
        return result
    }

    /**
     * Converts a 12-hour or 24-hour representation into 24-hour format (0-23) based on period indicators.
     */
    fun to24Hour(hour: Int, period: String?): Int {
        if (period.isNullOrBlank()) return hour
        val norm = period.trim().lowercase()
        val isPm = norm.contains("مساء") || norm.contains("مسا") || norm.contains("عصر") ||
                norm.contains("ليل") || norm.contains("ظه") || norm.contains("pm") || norm.contains("p.m")
        val isAm = norm.contains("صباح") || norm.contains("صبح") || norm.contains("فجر") ||
                norm.contains("am") || norm.contains("a.m")

        return when {
            isPm -> {
                if (hour in 1..11) hour + 12 else hour
            }
            isAm -> {
                if (hour == 12) 0 else hour
            }
            else -> hour
        }
    }

    /**
     * Calculates the timestamp in millis for the upcoming target day of the week.
     */
    private fun calculateNextWeekday(targetCalendarDay: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentDay = cal.get(Calendar.DAY_OF_WEEK)
        var daysToAdd = (targetCalendarDay - currentDay + 7) % 7
        if (daysToAdd == 0) {
            daysToAdd = 7 // Default to next week's occurrence if today is the same weekday
        }
        cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
        return cal.timeInMillis
    }

    /**
     * Resolves month name (Arabic or English) to a 0-indexed Calendar month (0 = Jan, 11 = Dec).
     */
    private fun parseMonthNameToCalendarMonth(monthStr: String): Int? {
        val norm = monthStr.trim().lowercase()
            .replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .replace("ة", "ه")

        return when {
            norm in listOf("يناير", "كانون الثاني", "january", "jan") -> Calendar.JANUARY
            norm in listOf("فبراير", "شباط", "february", "feb") -> Calendar.FEBRUARY
            norm in listOf("مارس", "اذار", "march", "mar") -> Calendar.MARCH
            norm in listOf("ابريل", "نيسان", "april", "apr") -> Calendar.APRIL
            norm in listOf("مايو", "ايار", "may") -> Calendar.MAY
            norm in listOf("يونيو", "حزيران", "june", "jun") -> Calendar.JUNE
            norm in listOf("يوليو", "تموز", "july", "jul") -> Calendar.JULY
            norm in listOf("اغسطس", "اب", "august", "aug") -> Calendar.AUGUST
            norm in listOf("سبتمبر", "ايلول", "september", "sep", "sept") -> Calendar.SEPTEMBER
            norm in listOf("اكتوبر", "تشرين الاول", "october", "oct") -> Calendar.OCTOBER
            norm in listOf("نوفمبر", "تشرين الثاني", "november", "nov") -> Calendar.NOVEMBER
            norm in listOf("ديسمبر", "كانون الاول", "december", "dec") -> Calendar.DECEMBER
            else -> null
        }
    }

    /**
     * Creates a normalized start-of-day timestamp in milliseconds.
     */
    private fun createDateMillis(day: Int, monthIndex: Int, year: Int? = null): Long {
        val currentCal = Calendar.getInstance()
        val targetYear = year ?: currentCal.get(Calendar.YEAR)

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, targetYear)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If no year was specified and the date is already in the past, roll to next year if appropriate
        if (year == null && cal.timeInMillis < DateTimeUtils.getTodayStartMillis() - (30L * 24 * 60 * 60 * 1000L)) {
            cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }

    /**
     * Parses a raw text message to extract date, weekdays, specific calendar dates (e.g. 14 سبتمبر or 14/9),
     * start time, end time, and a clean title.
     * Fully operates in 24-hour time format with complete support for AM/PM indicators ("3 مساء" -> 15:00).
     */
    fun parse(rawText: String): ParsedTaskResult {
        if (rawText.isBlank()) {
            return ParsedTaskResult(cleanTitle = "")
        }

        var workingText = normalizeArabicDigits(rawText.trim())
        var parsedDueDate: Long? = null
        var startHour: Int? = null
        var startMinute: Int? = null
        var endHour: Int? = null
        var endMinute: Int? = null
        var parsedRecurrence = RecurrenceType.NONE

        // 0. Recurrence Detection (e.g. "يومياً", "كل يوم", "أسبوعياً", "كل أسبوع", "شهرياً", "daily", "weekly", "monthly", "weekdays")
        val dailyRegex = Regex("""(?iu)(?:^|\s+)(يومياً|يوميا|كل\s+يوم|daily|every\s+day)(?:\s+|$)""")
        val weekdaysRegex = Regex("""(?iu)(?:^|\s+)(أيام\s+العمل|ايام\s+العمل|weekdays|every\s+weekday)(?:\s+|$)""")
        val weeklyRegex = Regex("""(?iu)(?:^|\s+)(أسبوعياً|اسبوعياً|أسبوعيا|اسبوعيا|كل\s+أسبوع|كل\s+اسبوع|weekly|every\s+week)(?:\s+|$)""")
        val monthlyRegex = Regex("""(?iu)(?:^|\s+)(شهرياً|شهريا|كل\s+شهر|monthly|every\s+month)(?:\s+|$)""")
        val yearlyRegex = Regex("""(?iu)(?:^|\s+)(سنوياً|سنويا|كل\s+سنة|كل\s+سنه|كل\s+عام|yearly|annually|every\s+year)(?:\s+|$)""")

        if (dailyRegex.containsMatchIn(workingText)) {
            parsedRecurrence = RecurrenceType.DAILY
            workingText = workingText.replace(dailyRegex, " ")
        } else if (weekdaysRegex.containsMatchIn(workingText)) {
            parsedRecurrence = RecurrenceType.WEEKDAYS
            workingText = workingText.replace(weekdaysRegex, " ")
        } else if (weeklyRegex.containsMatchIn(workingText)) {
            parsedRecurrence = RecurrenceType.WEEKLY
            workingText = workingText.replace(weeklyRegex, " ")
        } else if (monthlyRegex.containsMatchIn(workingText)) {
            parsedRecurrence = RecurrenceType.MONTHLY
            workingText = workingText.replace(monthlyRegex, " ")
        } else if (yearlyRegex.containsMatchIn(workingText)) {
            parsedRecurrence = RecurrenceType.YEARLY
            workingText = workingText.replace(yearlyRegex, " ")
        }

        // If recurrence is set and no date specified yet, default to today
        if (parsedRecurrence != RecurrenceType.NONE && parsedDueDate == null) {
            parsedDueDate = DateTimeUtils.getTodayStartMillis()
        }

        // 1. Check relative date keywords (today, tomorrow, day after tomorrow)
        val dayAfterTomorrowRegex = Regex("""(?iu)(?:^|\s+)(بعد\s+(?:غد|غداً|بكرة|باكر)|day\s+after\s+tomorrow)(?:\s+|$)""")
        val tomorrowRegex = Regex("""(?iu)(?:^|\s+)(غداً|غدا|بكرة|باكر|غد|tomorrow)(?:\s+|$)""")
        val todayRegex = Regex("""(?iu)(?:^|\s+)(اليوم|today|tonight|الليلة)(?:\s+|$)""")

        if (dayAfterTomorrowRegex.containsMatchIn(workingText)) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 2)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            parsedDueDate = cal.timeInMillis
            workingText = workingText.replace(dayAfterTomorrowRegex, " ")
        } else if (tomorrowRegex.containsMatchIn(workingText)) {
            parsedDueDate = DateTimeUtils.getTomorrowStartMillis()
            workingText = workingText.replace(tomorrowRegex, " ")
        } else if (todayRegex.containsMatchIn(workingText)) {
            parsedDueDate = DateTimeUtils.getTodayStartMillis()
            workingText = workingText.replace(todayRegex, " ")
        }

        // 2. Specific Named Month Dates (e.g. "بتاريخ 14 سبتمبر", "14 سبتمبر 2026", "14th of September", "September 14")
        if (parsedDueDate == null) {
            val monthNamesList = listOf(
                "يناير", "فبراير", "مارس", "أبريل", "ابريل", "مايو", "يونيو", "يوليو", "أغسطس", "اغسطس", "سبتمبر", "أكتوبر", "اكتوبر", "نوفمبر", "ديسمبر",
                "كانون الثاني", "شباط", "آذار", "اذار", "نيسان", "أيار", "ايار", "حزيران", "تموز", "آب", "اب", "أيلول", "ايلول", "تشرين الأول", "تشرين الاول", "تشرين الثاني", "كانون الأول", "كانون الاول",
                "january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december",
                "jan", "feb", "mar", "apr", "jun", "jul", "aug", "sep", "sept", "oct", "nov", "dec"
            ).joinToString("|")

            // Pattern A: Day followed by Month Name (e.g. "بتاريخ 14 سبتمبر", "14th September 2026", "يوم 14 من سبتمبر")
            val dayThenMonthRegex = Regex(
                """(?i)(?:بتاريخ|تاريخ|يوم|on|date:?)?\s*(\d{1,2})(?:st|nd|rd|th)?\s*(?:من|of)?\s*($monthNamesList)(?:\s*(\d{4}))?"""
            )
            val matchDayMonth = dayThenMonthRegex.find(workingText)
            if (matchDayMonth != null) {
                val day = matchDayMonth.groupValues[1].toIntOrNull()
                val monthStr = matchDayMonth.groupValues[2]
                val year = matchDayMonth.groupValues[3].toIntOrNull()
                val monthIndex = parseMonthNameToCalendarMonth(monthStr)

                if (day != null && day in 1..31 && monthIndex != null) {
                    parsedDueDate = createDateMillis(day, monthIndex, year)
                    workingText = workingText.replace(matchDayMonth.value, " ")
                }
            } else {
                // Pattern B: Month Name followed by Day (e.g. "September 14", "September 14, 2026")
                val monthThenDayRegex = Regex(
                    """(?i)(?:on\s+)?($monthNamesList)\s+(\d{1,2})(?:st|nd|rd|th)?(?:,?\s*(\d{4}))?"""
                )
                val matchMonthDay = monthThenDayRegex.find(workingText)
                if (matchMonthDay != null) {
                    val monthStr = matchMonthDay.groupValues[1]
                    val day = matchMonthDay.groupValues[2].toIntOrNull()
                    val year = matchMonthDay.groupValues[3].toIntOrNull()
                    val monthIndex = parseMonthNameToCalendarMonth(monthStr)

                    if (day != null && day in 1..31 && monthIndex != null) {
                        parsedDueDate = createDateMillis(day, monthIndex, year)
                        workingText = workingText.replace(matchMonthDay.value, " ")
                    }
                }
            }
        }

        // 3. Numerical Dates (e.g. "بتاريخ 14/9", "14/09", "14-9", "14/9/2026", "2026-09-14", "14-09-2026")
        if (parsedDueDate == null) {
            // Check DD/MM or DD/MM/YYYY or DD-MM or DD-MM-YYYY (with optional "بتاريخ" / "تاريخ" / "on")
            val numericalDateRegex = Regex(
                """(?i)(?:بتاريخ|تاريخ|on|date:?)?\s*\b(\d{1,2})[/.-](\d{1,2})(?:[/.-](\d{2,4}))?\b"""
            )
            val matchNum = numericalDateRegex.find(workingText)
            if (matchNum != null) {
                val p1 = matchNum.groupValues[1].toIntOrNull()
                val p2 = matchNum.groupValues[2].toIntOrNull()
                var rawYear = matchNum.groupValues[3].toIntOrNull()

                if (rawYear != null && rawYear < 100) {
                    rawYear += 2000
                }

                if (p1 != null && p2 != null) {
                    // Decide Day vs Month (standard Day/Month convention)
                    val (day, month) = if (p1 in 1..31 && p2 in 1..12) {
                        Pair(p1, p2)
                    } else if (p1 in 1..12 && p2 in 1..31) {
                        Pair(p2, p1)
                    } else {
                        Pair(null, null)
                    }

                    if (day != null && month != null) {
                        parsedDueDate = createDateMillis(day, month - 1, rawYear)
                        workingText = workingText.replace(matchNum.value, " ")
                    }
                }
            }
        }

        // 4. Weekdays Detection (e.g. "go to garage in next Sunday", "الأحد القادم", "يوم الثلاثاء", "next Friday", "on Monday")
        if (parsedDueDate == null) {
            val weekdayPattern = Regex(
                """(?i)(?:(?:in|on|at|for|في|يوم)\s+)?(?:next\s+|this\s+)?(الأحد|الاحد|الإثنين|الاثنين|التنين|الثلاثاء|الثلاثا|الأربعاء|الاربعاء|الاربعا|الخميس|الجمعة|الجمعه|السبت|sunday|monday|tuesday|wednesday|thursday|friday|saturday|sun|mon|tue|tues|wed|thu|thur|thurs|fri|sat)(?:\s+(?:القادم|المقبل|الجاى|الجاي|next))?"""
            )
            val matchWeekday = weekdayPattern.find(workingText)
            if (matchWeekday != null) {
                val dayStr = matchWeekday.groupValues[1].lowercase()
                val targetDayOfWeek = when {
                    dayStr in listOf("الأحد", "الاحد", "sunday", "sun") -> Calendar.SUNDAY
                    dayStr in listOf("الإثنين", "الاثنين", "التنين", "monday", "mon") -> Calendar.MONDAY
                    dayStr in listOf("الثلاثاء", "الثلاثا", "tuesday", "tue", "tues") -> Calendar.TUESDAY
                    dayStr in listOf("الأربعاء", "الاربعاء", "الاربعا", "wednesday", "wed") -> Calendar.WEDNESDAY
                    dayStr in listOf("الخميس", "thursday", "thu", "thur", "thurs") -> Calendar.THURSDAY
                    dayStr in listOf("الجمعة", "الجمعه", "friday", "fri") -> Calendar.FRIDAY
                    dayStr in listOf("السبت", "saturday", "sat") -> Calendar.SATURDAY
                    else -> null
                }

                if (targetDayOfWeek != null) {
                    parsedDueDate = calculateNextWeekday(targetDayOfWeek)
                    workingText = workingText.replace(matchWeekday.value, " ")
                }
            }
        }

        // 5. Relative time offsets (e.g., "بعد ساعتين", "بعد ساعة", "بعد 3 ساعات", "in 2 hours", "بعد 30 دقيقة")
        val inTwoHoursRegex = Regex("""(?i)\b(بعد\s+ساعتين|بعد\s+ساعتان|in\s+2\s+hours?)\b""")
        val inOneHourRegex = Regex("""(?i)\b(بعد\s+ساعة|بعد\s+ساعه|in\s+1\s+hour|in\s+an\s+hour)\b""")
        val inXHoursRegex = Regex("""(?i)\b(بعد\s+(\d+)\s+ساع(?:ات|ة)|in\s+(\d+)\s+hours?)\b""")
        val inXMinsRegex = Regex("""(?i)\b(بعد\s+(\d+)\s+دقيق(?:ة|ات|ق)|in\s+(\d+)\s+(?:mins?|minutes?))\b""")

        if (inTwoHoursRegex.containsMatchIn(workingText)) {
            val cal = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 2) }
            startHour = cal.get(Calendar.HOUR_OF_DAY)
            startMinute = cal.get(Calendar.MINUTE)
            parsedDueDate = parsedDueDate ?: DateTimeUtils.getTodayStartMillis()
            workingText = workingText.replace(inTwoHoursRegex, " ")
        } else if (inOneHourRegex.containsMatchIn(workingText)) {
            val cal = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }
            startHour = cal.get(Calendar.HOUR_OF_DAY)
            startMinute = cal.get(Calendar.MINUTE)
            parsedDueDate = parsedDueDate ?: DateTimeUtils.getTodayStartMillis()
            workingText = workingText.replace(inOneHourRegex, " ")
        } else {
            val matchXHours = inXHoursRegex.find(workingText)
            if (matchXHours != null) {
                val hours = (matchXHours.groupValues[2].ifEmpty { matchXHours.groupValues[3] }).toIntOrNull() ?: 1
                val cal = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, hours) }
                startHour = cal.get(Calendar.HOUR_OF_DAY)
                startMinute = cal.get(Calendar.MINUTE)
                parsedDueDate = parsedDueDate ?: DateTimeUtils.getTodayStartMillis()
                workingText = workingText.replace(matchXHours.value, " ")
            } else {
                val matchXMins = inXMinsRegex.find(workingText)
                if (matchXMins != null) {
                    val mins = (matchXMins.groupValues[2].ifEmpty { matchXMins.groupValues[3] }).toIntOrNull() ?: 15
                    val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, mins) }
                    startHour = cal.get(Calendar.HOUR_OF_DAY)
                    startMinute = cal.get(Calendar.MINUTE)
                    parsedDueDate = parsedDueDate ?: DateTimeUtils.getTodayStartMillis()
                    workingText = workingText.replace(matchXMins.value, " ")
                }
            }
        }

        // Period marker keyword helper
        val periodWords = """(?:مساءً|مساء|مسا|عصراً|عصرا|عصر|ليلاً|ليلا|ليل|ظهراً|ظهرا|ظهر|صباحاً|صباحا|صبح|فجراً|فجرا|فجر|pm|p\.m\.|am|a\.m\.)"""

        // 6. Time Range Detection (e.g. "من 3 إلى 5 مساء", "من 10 صباحا إلى 2 ظهرا", "from 3 to 5 pm", "15:00 - 22:00")
        if (startHour == null) {
            val rangeRegex = Regex(
                """(?i)(?:من\s+(?:الساعة\s+)?)?(\d{1,2})(?::(\d{2}))?\s*($periodWords)?\s*(?:إلى|الى|حتى|لـ|to|-)\s*(?:(?:الساعة\s+)?)?(\d{1,2})(?::(\d{2}))?\s*($periodWords)?"""
            )
            val rangeMatch = rangeRegex.find(workingText)
            if (rangeMatch != null) {
                val sH = rangeMatch.groupValues[1].toIntOrNull()
                val sM = rangeMatch.groupValues[2].ifEmpty { "0" }.toIntOrNull() ?: 0
                val sPeriod = rangeMatch.groupValues[3].ifEmpty { null }

                val eH = rangeMatch.groupValues[4].toIntOrNull()
                val eM = rangeMatch.groupValues[5].ifEmpty { "0" }.toIntOrNull() ?: 0
                val ePeriod = rangeMatch.groupValues[6].ifEmpty { null }

                if (sH != null && eH != null) {
                    // Apply periods
                    val convertedEnd = to24Hour(eH, ePeriod)
                    val convertedStart = if (sPeriod != null) {
                        to24Hour(sH, sPeriod)
                    } else if (ePeriod != null && ePeriod.contains(Regex("""(?i)(مساء|مسا|عصر|ليل|pm|p\.m)"""))) {
                        // Inherit PM to start if reasonable (e.g. "من 3 إلى 5 مساء" -> 15 to 17)
                        if (sH < 8 && sH < eH) to24Hour(sH, ePeriod) else sH
                    } else {
                        sH
                    }

                    if (convertedStart in 0..23 && convertedEnd in 0..23) {
                        startHour = convertedStart
                        startMinute = sM.coerceIn(0, 59)
                        endHour = convertedEnd
                        endMinute = eM.coerceIn(0, 59)
                        parsedDueDate = parsedDueDate ?: DateTimeUtils.getTodayStartMillis()
                        workingText = workingText.replace(rangeMatch.value, " ")
                    }
                }
            }
        }

        // 7. Single Time with Period Indicator (e.g., "3 مساء", "3 مساءً", "الساعة 3 عصرا", "3:30 pm", "10 صباحا", "8 ليلا")
        if (startHour == null) {
            val singleWithPeriodRegex = Regex(
                """(?i)(?:(?:الساعة|الساعه|at|في تمام)\s+)?(\d{1,2})(?::(\d{2}))?\s*($periodWords)"""
            )
            val matchPeriod = singleWithPeriodRegex.find(workingText)
            if (matchPeriod != null) {
                val rawH = matchPeriod.groupValues[1].toIntOrNull()
                val rawM = matchPeriod.groupValues[2].ifEmpty { "0" }.toIntOrNull() ?: 0
                val periodStr = matchPeriod.groupValues[3]

                if (rawH != null) {
                    val convertedH = to24Hour(rawH, periodStr)
                    if (convertedH in 0..23) {
                        startHour = convertedH
                        startMinute = rawM.coerceIn(0, 59)
                        parsedDueDate = parsedDueDate ?: DateTimeUtils.getTodayStartMillis()
                        workingText = workingText.replace(matchPeriod.value, " ")
                    }
                }
            }
        }

        // 8. Single 24-Hour Time Detection (e.g., "الساعة 15:30", "الساعة 18", "at 14:00", "16:45", "at 13:00")
        if (startHour == null) {
            val singleTimeWithPrefixRegex = Regex("""(?i)(?:الساعة|الساعه|at|في تمام)\s+(\d{1,2})(?::(\d{2}))?""")
            val singlePrefixMatch = singleTimeWithPrefixRegex.find(workingText)
            if (singlePrefixMatch != null) {
                val h = singlePrefixMatch.groupValues[1].toIntOrNull()
                val m = singlePrefixMatch.groupValues[2].ifEmpty { "0" }.toIntOrNull() ?: 0
                if (h != null && h in 0..23) {
                    startHour = h
                    startMinute = m.coerceIn(0, 59)
                    parsedDueDate = parsedDueDate ?: DateTimeUtils.getTodayStartMillis()
                    workingText = workingText.replace(singlePrefixMatch.value, " ")
                }
            } else {
                // Standalone time formatted as HH:mm (e.g. 15:30, 08:00, 13:00)
                val standaloneTimeRegex = Regex("""\b([01]?\d|2[0-3]):([0-5]\d)\b""")
                val standaloneMatch = standaloneTimeRegex.find(workingText)
                if (standaloneMatch != null) {
                    val h = standaloneMatch.groupValues[1].toIntOrNull()
                    val m = standaloneMatch.groupValues[2].toIntOrNull() ?: 0
                    if (h != null && h in 0..23) {
                        startHour = h
                        startMinute = m.coerceIn(0, 59)
                        parsedDueDate = parsedDueDate ?: DateTimeUtils.getTodayStartMillis()
                        workingText = workingText.replace(standaloneMatch.value, " ")
                    }
                }
            }
        }

        // Clean extra connectors, prepositions and whitespace from the title
        var cleanTitle = workingText
            .replace(Regex("""(?iu)(?:^|\s+)(?:بتاريخ|تاريخ|في تمام|في|at|on|الساعة|الساعه)(?:\s+|$)"""), " ")
            .replace(Regex("""(?iu)(?:^|\s+)(?:بتاريخ|تاريخ|في تمام|في|at|on|الساعة|الساعه)(?:\s+|$)"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim(' ', '-', ':', '،', ',', '.')

        // If title became completely empty, fallback to rawText
        if (cleanTitle.isBlank()) {
            cleanTitle = rawText.trim()
        }

        return ParsedTaskResult(
            cleanTitle = cleanTitle,
            dueDateMillis = parsedDueDate,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            recurrence = parsedRecurrence
        )
    }
}
