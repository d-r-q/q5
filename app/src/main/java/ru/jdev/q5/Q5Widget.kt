package ru.jdev.q5

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews


/**
 * Implementation of App Widget functionality.
 */
class Q5Widget : AppWidgetProvider() {


    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {

        val buttonViews = arrayOf(
                R.id.button11,
                R.id.button12,
                R.id.button13,
                R.id.button14,
                R.id.button21,
                R.id.button22,
                R.id.button23,
                R.id.button24,
                R.id.button31,
                R.id.button32,
                R.id.button33,
                R.id.button34,
                R.id.button41,
                R.id.button42,
                R.id.button43,
                R.id.button44
        )
        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {

            val views = RemoteViews(context.packageName, R.layout.q5_widget)

            for (i in 0..15) {
                Log.d("updateAppWidget", "Updating $i-th button")
                val configIntent = Intent(context, EnterSumActivity::class.java)
                val category = Categories.categories[i]
                configIntent.action = category
                configIntent.putExtra("category", category)
                configIntent.putExtra(EnterSumActivity.sourceExtra, "manual")
                val configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
                views.setOnClickPendingIntent(buttonViews[i], configPendingIntent)
                views.setTextViewText(buttonViews[i], category)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

