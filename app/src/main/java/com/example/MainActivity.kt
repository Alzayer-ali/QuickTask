package com.example

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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

    private val initialPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val calendarGranted = permissions[Manifest.permission.READ_CALENDAR] == true &&
                permissions[Manifest.permission.WRITE_CALENDAR] == true
        if (calendarGranted) {
            calendarObserver.startObserving()
            viewModel.syncFromCalendar()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as TaskApplication).repository
        calendarObserver = CalendarObserverManager(this, repository)

        requestInitialPermissionsIfFirstLaunch()

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

    private fun requestInitialPermissionsIfFirstLaunch() {
        val prefs = getSharedPreferences(PREFS_APP_LAUNCH, Context.MODE_PRIVATE)
        val hasRequested = prefs.getBoolean(KEY_HAS_REQUESTED_INITIAL_PERMS, false)
        if (!hasRequested) {
            prefs.edit().putBoolean(KEY_HAS_REQUESTED_INITIAL_PERMS, true).apply()

            val permissionsToRequest = mutableListOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            initialPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    companion object {
        const val PREFS_APP_LAUNCH = "app_launch_prefs"
        const val KEY_HAS_REQUESTED_INITIAL_PERMS = "has_requested_initial_permissions"
    }
}


