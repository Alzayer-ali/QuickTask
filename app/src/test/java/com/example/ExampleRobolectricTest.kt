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
        assertEquals("Task Manager", appName)
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

