// ==========================================
// IDENTITY: The Alarm Trigger / BroadcastReceiver
// FILEPATH: app/src/main/java/com/example/pocket_orbit/network/TaskAlarmReceiver.kt
// VERSION: 1.0.0
// VIBE: Strict timing. No excuses. 🚨
// ==========================================

package com.example.pocket_orbit.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntOfExtra("TASK_ID", -1)
        val title = intent.getStringExtra("TASK_TITLE") ?: "Task Reminder"
        val subject = intent.getStringExtra("TASK_SUBJECT") ?: "Orbit"

        if (taskId != -1) {
            val notificationHelper = NotificationHelper(context)
            notificationHelper.showTaskNotification(
                taskId = taskId,
                title = "[$subject] Time to Grind! 🔥",
                content = "Your task \"$title\" starts NOW. No slippage allowed."
            )
        }
    }

    private fun Intent.getIntOfExtra(name: String, defaultValue: Int): Int {
        return getIntExtra(name, defaultValue)
    }
}