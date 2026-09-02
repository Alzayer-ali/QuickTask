package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.sync.CalendarObserverManager
import com.example.ui.DashboardScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.TaskManagerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.provideFactory(
            (application as TaskApplication).repository
        )
    }

    private lateinit var calendarObserver: CalendarObserverManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as TaskApplication).repository
        calendarObserver = CalendarObserverManager(this, repository)

        setContent {
            TaskManagerTheme {
                DashboardScreen(
                    viewModel = viewModel,
                    onCalendarPermissionGranted = {
                        calendarObserver.startObserving()
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        calendarObserver.startObserving()
    }

    override fun onResume() {
        super.onResume()
        // Automatically sync any edits made in Google Calendar / phone calendar while app was in background
        viewModel.syncFromCalendar()
    }

    override fun onStop() {
        super.onStop()
        calendarObserver.stopObserving()
    }
}


