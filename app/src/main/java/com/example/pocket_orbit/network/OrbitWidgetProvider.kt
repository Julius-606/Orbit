package com.example.pocket_orbit.network

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.pocket_orbit.MainActivity
import com.example.pocket_orbit.R

class OrbitWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        widgetIds: IntArray
    ) {
        widgetIds.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_orbit).apply {
                setTextViewText(R.id.widget_title, "Orbit")
                setTextViewText(R.id.widget_status, "Tap to open your Life-OS")
                val intent = PendingIntent.getActivity(
                    context,
                    widgetId,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_root, intent)
            }
            manager.updateAppWidget(widgetId, views)
        }
    }
}
