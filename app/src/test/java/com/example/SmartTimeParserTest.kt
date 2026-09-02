package com.example

import com.example.util.DateTimeUtils
import com.example.util.SmartTimeParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar

class SmartTimeParserTest {

    @Test
    fun testParseArabicNamedDate() {
        val input = "الذهاب للحديقة بتاريخ 14 سبتمبر"
        val result = SmartTimeParser.parse(input)

        assertEquals("الذهاب للحديقة", result.cleanTitle)
        assertNotNull(result.dueDateMillis)

        val cal = Calendar.getInstance().apply { timeInMillis = result.dueDateMillis!! }
        assertEquals(14, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.SEPTEMBER, cal.get(Calendar.MONTH))
    }

    @Test
    fun testParseSlashDate() {
        val input = "الذهاب للحديقة 14/9"
        val result = SmartTimeParser.parse(input)

        assertEquals("الذهاب للحديقة", result.cleanTitle)
        assertNotNull(result.dueDateMillis)

        val cal = Calendar.getInstance().apply { timeInMillis = result.dueDateMillis!! }
        assertEquals(14, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.SEPTEMBER, cal.get(Calendar.MONTH))
    }

    @Test
    fun testParseDateWithTime() {
        val input = "الذهاب للحديقة 14-09 الساعة 4 عصرا"
        val result = SmartTimeParser.parse(input)

        assertEquals("الذهاب للحديقة", result.cleanTitle)
        assertEquals(16, result.startHour)
        assertEquals(0, result.startMinute)
        assertNotNull(result.dueDateMillis)

        val cal = Calendar.getInstance().apply { timeInMillis = result.dueDateMillis!! }
        assertEquals(14, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.SEPTEMBER, cal.get(Calendar.MONTH))
    }

    @Test
    fun testParseNextSundayInEnglish() {
        val input = "go to garage in next Sunday at 13:00"
        val result = SmartTimeParser.parse(input)

        assertEquals("go to garage", result.cleanTitle)
        assertEquals(13, result.startHour)
        assertEquals(0, result.startMinute)
        assertNotNull(result.dueDateMillis)

        val cal = Calendar.getInstance().apply { timeInMillis = result.dueDateMillis!! }
        assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun testParseNextSundayInArabicWithPM() {
        val input = "الأحد القادم الساعة 3 مساء صيانة السيارة"
        val result = SmartTimeParser.parse(input)

        assertEquals("صيانة السيارة", result.cleanTitle)
        assertEquals(15, result.startHour) // 3 مساء -> 15
        assertEquals(0, result.startMinute)
        assertNotNull(result.dueDateMillis)

        val cal = Calendar.getInstance().apply { timeInMillis = result.dueDateMillis!! }
        assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun testParseArabicPMTime() {
        val input = "اجتماع عمل 3 مساء"
        val result = SmartTimeParser.parse(input)

        assertEquals("اجتماع عمل", result.cleanTitle)
        assertEquals(15, result.startHour)
        assertEquals(0, result.startMinute)
    }

    @Test
    fun testParseArabicRangeWithPM() {
        val input = "مذاكرة من 3 إلى 5 مساء"
        val result = SmartTimeParser.parse(input)

        assertEquals("مذاكرة", result.cleanTitle)
        assertEquals(15, result.startHour)
        assertEquals(17, result.endHour)
    }

    @Test
    fun testParseRangeArabicWithTomorrow() {
        val input = "غدا من الساعة 15 إلى الساعة 22 مذاكرة رياضيات"
        val result = SmartTimeParser.parse(input)

        assertEquals("مذاكرة رياضيات", result.cleanTitle)
        assertEquals(15, result.startHour)
        assertEquals(0, result.startMinute)
        assertEquals(22, result.endHour)
        assertEquals(0, result.endMinute)
        assertNotNull(result.dueDateMillis)
    }

    @Test
    fun testParseArabicNumerals() {
        val input = "من الساعة ١٥ إلى الساعة ٢٢ قراءة كتاب"
        val result = SmartTimeParser.parse(input)

        assertEquals("قراءة كتاب", result.cleanTitle)
        assertEquals(15, result.startHour)
        assertEquals(22, result.endHour)
    }

    @Test
    fun testParseSingleTime() {
        val input = "اجتماع الساعة 14:30"
        val result = SmartTimeParser.parse(input)

        assertEquals("اجتماع", result.cleanTitle)
        assertEquals(14, result.startHour)
        assertEquals(30, result.startMinute)
    }

    @Test
    fun test24HourFormatDisplay() {
        val formatted = DateTimeUtils.formatDueDate(
            dueDateMillis = System.currentTimeMillis(),
            startHour = 15,
            startMinute = 0,
            endHour = 22,
            endMinute = 0
        )
        assert(formatted.contains("15:00 → 22:00"))
    }

    @Test
    fun testParseRecurrenceKeywords() {
        val dailyResult = SmartTimeParser.parse("أذكار الصباح يومياً الساعة 7:00")
        assertEquals(com.example.data.RecurrenceType.DAILY, dailyResult.recurrence)
        assertEquals(7, dailyResult.startHour)

        val weeklyResult = SmartTimeParser.parse("مراجعة المشاريع أسبوعياً يوم الأحد")
        assertEquals(com.example.data.RecurrenceType.WEEKLY, weeklyResult.recurrence)

        val monthlyResult = SmartTimeParser.parse("دفع الفواتير شهرياً")
        assertEquals(com.example.data.RecurrenceType.MONTHLY, monthlyResult.recurrence)
    }
}
