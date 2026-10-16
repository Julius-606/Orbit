// ==========================================
// IDENTITY: The Alert Hub / NotificationHelper
// FILEPATH: app/src/main/java/com/example/pocket_orbit/network/NotificationHelper.kt
// ==========================================

package com.example.pocket_orbit.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.pocket_orbit.R

object NotificationHelper {
    private const val CHANNEL_ID = "orbit_reminders_channel"
    private const val CHANNEL_NAME = "Orbit Life-OS Reminders"

    fun showTaskReminder(context: Context, taskId: Int, title: String, subject: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Task reminders and syllabus alerts from Orbit"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.orbit_icon)
            .setContentTitle("🪐 Orbit Alert: $title")
            .setContentText("Focus on $subject. Let's secure this W!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskId, notification)
    }

    fun scheduleAlarm(context: Context, taskId: Int, title: String, subject: String, triggerAtMillis: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
            val intent = android.content.Intent(context, TaskAlarmReceiver::class.java).apply {
                putExtra("TASK_ID", taskId)
                putExtra("TASK_TITLE", title)
                putExtra("TASK_SUBJECT", subject)
            }
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
            android.util.Log.d("NotificationHelper", "Scheduled reminder alarm for task $taskId at $triggerAtMillis")
        } catch (e: Exception) {
            android.util.Log.w("NotificationHelper", "Could not set exact alarm: ${e.message}")
        }
    }

    fun cancelAlarm(context: Context, taskId: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
            val intent = android.content.Intent(context, TaskAlarmReceiver::class.java)
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            android.util.Log.d("NotificationHelper", "Cancelled alarm for task $taskId")
        } catch (e: Exception) {
            android.util.Log.w("NotificationHelper", "Error cancelling alarm for task $taskId: ${e.message}")
        }
    }
}
