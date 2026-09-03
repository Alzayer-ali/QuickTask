package com.example.quicksetting

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.QuickCaptureActivity
import com.example.R
import com.example.SendTaskNotificationActivity
import com.example.util.QuickSettingClickAction
import com.example.util.QuickSettingPreferences

/**
 * Quick Settings Tile service that provides 1-tap quick task entry directly
 * from the Android system Quick Settings panel.
 *
 * Tapping the tile collapses the Quick Settings panel and executes the user-selected action:
 * - NOTIFICATION: Displays the Quick Task notification with direct text reply in the status bar.
 * - DIALOG: Opens the Quick Capture task creation dialog directly.
 */
class QuickTaskTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        val action = QuickSettingPreferences.getInstance(this).getAction()

        when (action) {
            QuickSettingClickAction.NOTIFICATION -> {
                val intent = Intent(this, SendTaskNotificationActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                launchActivityAndCollapse(intent)
            }
            QuickSettingClickAction.DIALOG -> {
                val intent = Intent(this, QuickCaptureActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                launchActivityAndCollapse(intent)
            }
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        tile.state = Tile.STATE_INACTIVE
        tile.label = getString(R.string.quick_task_tile_label)
        tile.icon = Icon.createWithResource(this, R.drawable.ic_task_notification)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val action = QuickSettingPreferences.getInstance(this).getAction()
            tile.subtitle = if (action == QuickSettingClickAction.NOTIFICATION) {
                getString(R.string.quick_setting_action_notification)
            } else {
                getString(R.string.quick_setting_action_dialog)
            }
        }

        tile.updateTile()
    }

    private fun launchActivityAndCollapse(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
