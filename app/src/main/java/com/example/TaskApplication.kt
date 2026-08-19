package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.TaskRepository

class TaskApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: TaskRepository by lazy { TaskRepository(database.taskDao(), this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: TaskApplication
            private set
    }
}
