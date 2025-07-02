package com.charlesvonlehe.charlietracker

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.edit

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [CharlieTrackerWidgetConfigureActivity]
 */

private const val WIDGET_TAP_ACTION = "com.charlesvonlehe.CHARLIE_TRACKER_TAP"
private const val WIDGET_RESET_ACTION = "com.charlesvonlehe.CHARLIE_TRACKER_RESET"
private const val PREFS_NAME = "com.charlesvonlehe.charlietracker.CharlieTrackerWidget"
private const val WIDGET_COUNT_PREFS_KEY = "WIDGET_COUNT_PREFS_KEY"
private const val WIDGET_ENABLED_PREFS_KEY = "WIDGET_ENABLED_PREFS_KEY"

class CharlieTrackerWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deleteTitlePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, CharlieTrackerWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
        if (intent.action == WIDGET_TAP_ACTION) {
            // Update all widgets
            // Flip state
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isEnabled = prefs.getBoolean(WIDGET_ENABLED_PREFS_KEY, true)
            if(isEnabled) {
                val count = prefs.getInt(WIDGET_COUNT_PREFS_KEY, 0)
                prefs.edit { putInt(WIDGET_COUNT_PREFS_KEY, count + 1) }
            }
            prefs.edit { putBoolean(WIDGET_ENABLED_PREFS_KEY, !isEnabled) }

        }else if(intent.action == WIDGET_RESET_ACTION) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit {
                    putBoolean(WIDGET_ENABLED_PREFS_KEY, true)
                        .putInt(WIDGET_COUNT_PREFS_KEY, 0)
                }
        }

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.charlie_tracker_widget)
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val count = prefs.getInt(WIDGET_COUNT_PREFS_KEY, 0)
    val isEnabled = prefs.getBoolean(WIDGET_ENABLED_PREFS_KEY, true)
    views.setInt(R.id.text_container, "setBackgroundResource", if(isEnabled) { R.drawable.rounded_enabled } else { R.drawable.rounded_disabled })
    views.setTextViewText(R.id.appwidget_text, "$count")
    val intent = Intent(context, CharlieTrackerWidget::class.java).apply {
        action = WIDGET_TAP_ACTION
    }
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    views.setOnClickPendingIntent(R.id.charlie_tracker_widget_container, pendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}