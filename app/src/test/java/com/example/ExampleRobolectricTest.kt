package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.Priority
import com.example.data.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Quick Task", appName)
    }

    @Test
    fun `test backup JSON export and import`() {
        val originalTasks = listOf(
            TaskEntity(
                id = 1L,
                title = "مهمة اختبار يومية",
                notes = "ملاحظات هامة",
                dueDateMillis = System.currentTimeMillis(),
                dueTimeHour = 15,
                dueTimeMinute = 30,
                endTimeHour = 17,
                endTimeMinute = 0,
                priority = Priority.HIGH,
                category = "Work",
                recurrence = com.example.data.RecurrenceType.DAILY
            )
        )

        val json = com.example.util.TaskBackupHelper.exportToJson(originalTasks)
        assertNotNull(json)
        assertTrue(json.contains("مهمة اختبار يومية"))
        assertTrue(json.contains("DAILY"))

        val restoredTasks = com.example.util.TaskBackupHelper.parseFromJson(json)
        assertEquals(1, restoredTasks.size)
        assertEquals("مهمة اختبار يومية", restoredTasks[0].title)
        assertEquals(15, restoredTasks[0].dueTimeHour)
        assertEquals(17, restoredTasks[0].endTimeHour)
        assertEquals(com.example.data.RecurrenceType.DAILY, restoredTasks[0].recurrence)
    }

    @Test
    fun `test recurring task advances on completion`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<TaskApplication>()
        val repo = app.repository

        val initialDueDate = com.example.util.DateTimeUtils.getTodayStartMillis()
        val taskId = repo.insertTask(
            TaskEntity(
                title = "أذكار الصباح يومياً",
                dueDateMillis = initialDueDate,
                recurrence = com.example.data.RecurrenceType.DAILY
            )
        )

        val inserted = repo.allTasks.first().find { it.id == taskId }
        assertNotNull(inserted)
        assertEquals(false, inserted?.isCompleted)

        // Toggle completion
        repo.toggleTaskCompletion(inserted!!)

        val updated = repo.allTasks.first().find { it.id == taskId }
        assertNotNull(updated)
        // Recurring task should not be marked permanently completed, but its dueDate should advance!
        assertEquals(false, updated?.isCompleted)
        assertTrue(updated!!.dueDateMillis!! > initialDueDate)
    }

    @Test
    fun `insert and retrieve task in repository`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<TaskApplication>()
        val repo = app.repository

        val taskId = repo.insertTask(
            TaskEntity(
                title = "Test Lockscreen Task",
                notes = "Quick note",
                priority = Priority.HIGH,
                category = "Urgent"
            )
        )

        assertTrue(taskId > 0)
        val allTasks = repo.allTasks.first()
        val created = allTasks.find { it.id == taskId }
        assertNotNull(created)
        assertEquals("Test Lockscreen Task", created?.title)
        assertEquals(Priority.HIGH, created?.priority)
    }

    @Test
    fun `test TaskNotificationManager channel and show notification`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = com.example.notification.TaskNotificationManager(context)
        manager.showQuickTaskNotification()
        manager.cancelNotification()
    }
}

