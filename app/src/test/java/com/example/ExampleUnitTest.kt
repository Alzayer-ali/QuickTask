package com.example

import com.example.data.Priority
import com.example.data.RecurrenceType
import com.example.data.TaskEntity
import com.example.notification.TaskReminderReceiver
import com.example.util.DateTimeUtils
import com.example.util.TaskBackupHelper
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `test priority levels weighting`() {
        assertTrue(Priority.HIGH.level > Priority.MEDIUM.level)
        assertTrue(Priority.MEDIUM.level > Priority.LOW.level)
        assertEquals(3, Priority.HIGH.level)
        assertEquals(2, Priority.MEDIUM.level)
        assertEquals(1, Priority.LOW.level)
    }

    @Test
    fun `test recurrence date advancement is strictly in the future`() {
        val today = DateTimeUtils.getTodayStartMillis()

        // DAILY recurrence should advance past today
        val nextDaily = DateTimeUtils.calculateNextRecurrenceDate(today, RecurrenceType.DAILY)
        assertTrue(nextDaily > today)

        // WEEKLY recurrence should advance by at least 1 week
        val nextWeekly = DateTimeUtils.calculateNextRecurrenceDate(today, RecurrenceType.WEEKLY)
        assertTrue(nextWeekly >= today + 7 * 24 * 60 * 60 * 1000L)

        // MONTHLY recurrence should advance past weekly
        val nextMonthly = DateTimeUtils.calculateNextRecurrenceDate(today, RecurrenceType.MONTHLY)
        assertTrue(nextMonthly > nextWeekly)
    }

    @Test
    fun `test reminder notification id offset avoids collision with quick task notification`() {
        // Quick task notification ID is 1001
        val reminderId1 = TaskReminderReceiver.getReminderNotificationId(1L)
        val reminderId1001 = TaskReminderReceiver.getReminderNotificationId(1001L)

        assertTrue(reminderId1 >= 10000)
        assertTrue(reminderId1001 >= 10000)
        assertNotEquals(1001, reminderId1)
        assertNotEquals(1001, reminderId1001)
    }
}
