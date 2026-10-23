package com.example.pocket_orbit.network

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.pocket_orbit.MainActivity
import com.example.pocket_orbit.R
import com.example.pocket_orbit.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class OrbitWidgetProvider : AppWidgetProvider() {

    private val widgetScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        widgetIds: IntArray
    ) {
        val appContext = context.applicationContext

        widgetScope.launch {
            val db = AppDatabase.getDatabase(appContext)
            val nextTask = try {
                db.studyTaskDao().getNextPendingTask()
            } catch (e: Exception) {
                null
            }
            val pendingCount = try {
                db.studyTaskDao().getPendingCountDirect()
            } catch (e: Exception) {
                0
            }

            widgetIds.forEach { widgetId ->
                val views = RemoteViews(appContext.packageName, R.layout.widget_orbit).apply {
                    setTextViewText(R.id.widget_title, "POCKET ORBIT 🪐")
                    setTextViewText(R.id.widget_status, "ONLINE")

                    if (nextTask != null) {
                        setTextViewText(R.id.widget_task_label, "NEXT FOCUS TASK")
                        setTextViewText(R.id.widget_task_title, nextTask.title)
                        setTextViewText(
                            R.id.widget_pending_count,
                            "$pendingCount pending action(s) • ${nextTask.subject}"
                        )
                    } else {
                        setTextViewText(R.id.widget_task_label, "ORBIT LIFE-OS")
                        setTextViewText(R.id.widget_task_title, "All Tasks Cleared! 🎯")
                        setTextViewText(R.id.widget_pending_count, "Forex Guardian & Med Pulse Active")
                    }

                    val launchIntent = PendingIntent.getActivity(
                        appContext,
                        widgetId,
                        Intent(appContext, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        },
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    setOnClickPendingIntent(R.id.widget_root, launchIntent)
                    setOnClickPendingIntent(R.id.widget_action_launch, launchIntent)
                }

                manager.updateAppWidget(widgetId, views)
            }
        }
    }
}
