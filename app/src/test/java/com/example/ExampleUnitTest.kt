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

    @Test
    fun `test quick setting click action options and fallback`() {
        val notifAction = com.example.util.QuickSettingClickAction.valueOf("NOTIFICATION")
        val dialogAction = com.example.util.QuickSettingClickAction.valueOf("DIALOG")

        assertEquals(com.example.util.QuickSettingClickAction.NOTIFICATION, notifAction)
        assertEquals(com.example.util.QuickSettingClickAction.DIALOG, dialogAction)

        assertTrue(notifAction.title.isNotBlank())
        assertTrue(notifAction.description.isNotBlank())

        assertTrue(dialogAction.title.isNotBlank())
        assertTrue(dialogAction.description.isNotBlank())

        assertNotEquals(notifAction.title, dialogAction.title)
    }

    @Test
    fun `test widget theme style enum options and labels`() {
        val followApp = com.example.ui.theme.WidgetThemeStyle.FOLLOW_APP
        val liquidGlass = com.example.ui.theme.WidgetThemeStyle.LIQUID_GLASS

        assertEquals("نفس مظهر التطبيق", followApp.label)
        assertTrue(liquidGlass.label.contains("زجاجي شفاف"))
        assertTrue(liquidGlass.label.contains("Liquid glass"))

        // Verify default in ThemeSettings
        val defaultSettings = com.example.ui.theme.ThemeSettings()
        assertEquals(com.example.ui.theme.WidgetThemeStyle.FOLLOW_APP, defaultSettings.widgetStyle)

        // Verify copy with liquid glass
        val customSettings = defaultSettings.copy(widgetStyle = com.example.ui.theme.WidgetThemeStyle.LIQUID_GLASS)
        assertEquals(com.example.ui.theme.WidgetThemeStyle.LIQUID_GLASS, customSettings.widgetStyle)
    }

    @Test
    fun `test initial permissions constants in MainActivity`() {
        assertEquals("app_launch_prefs", MainActivity.PREFS_APP_LAUNCH)
        assertEquals("has_requested_initial_permissions", MainActivity.KEY_HAS_REQUESTED_INITIAL_PERMS)
    }
}
