// ==========================================
// IDENTITY: The Watchdog / TaskAlarmReceiver
// FILEPATH: app/src/main/java/com/example/pocket_orbit/network/TaskAlarmReceiver.kt
// ==========================================

package com.example.pocket_orbit.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("TASK_ID", 0)
        val title = intent.getStringExtra("TASK_TITLE") ?: "Pending Task"
        val subject = intent.getStringExtra("TASK_SUBJECT") ?: "Life Admin"

        Log.i("TaskAlarmReceiver", "Alarm fired for task $taskId: $title")
        NotificationHelper.showTaskReminder(context, taskId, title, subject)
    }
}
