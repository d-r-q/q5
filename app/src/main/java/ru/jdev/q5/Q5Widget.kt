package ru.jdev.q5

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.util.Log
import android.widget.RemoteViews
import android.graphics.RadialGradient

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

        val textViews = arrayOf(
                R.id.label11,
                R.id.label12,
                R.id.label13,
                R.id.label14,
                R.id.label21,
                R.id.label22,
                R.id.label23,
                R.id.label24,
                R.id.label31,
                R.id.label32,
                R.id.label33,
                R.id.label34,
                R.id.label41,
                R.id.label42,
                R.id.label43,
                R.id.label44
        )

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {

            val categories = Categories(context)
            val views = RemoteViews(context.packageName, R.layout.q5_widget)

            for (i in 0..15) {
                val category = categories.names[i]
                views.setImageViewBitmap(buttonViews[i], getCategoryImage(categories.names[i]))
                val configIntent = Intent(context, EnterSumActivity::class.java)
                configIntent.action = category
                configIntent.putExtra("category", category)
                configIntent.putExtra(EnterSumActivity.sourceExtra, "manual")
                val configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
                views.setOnClickPendingIntent(buttonViews[i], configPendingIntent)
                views.setOnClickPendingIntent(textViews[i], configPendingIntent)
                views.setTextViewText(textViews[i], category)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun getCategoryImage(category: String): Bitmap? {
            val IMAGE_WIDTH = 160
            val IMAGE_HEIGHT = 160

            val lighterYellow = Color.argb(255, 255, 230, 0)
            val darkerYellow = Color.argb(255, 229, 180, 0)
            val g = LinearGradient(80.0F, 0.0F, 80.0F, 160.0F, lighterYellow, darkerYellow, Shader.TileMode.CLAMP)

            val conf = Bitmap.Config.ARGB_8888
            val bmp = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, conf)
            val canvas = Canvas(bmp)

            val background = Paint()
            background.setARGB(255, 255, 230, 0)

            val strokeGradient = RadialGradient(20f, 20f, 200f, darkerYellow, lighterYellow,
                    android.graphics.Shader.TileMode.CLAMP)
            val stroke = Paint()
            stroke.setARGB(255, 229, 180, 0)
            stroke.isDither = true
            stroke.shader = strokeGradient


            val foreground = Paint()
            foreground.setARGB(255, 255, 255, 255)
            foreground.style = Paint.Style.FILL
            foreground.color = Color.argb(209, 112, 86, 0)
            foreground.isSubpixelText = true
            foreground.textSize = 90F
            foreground.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

            canvas.drawOval(RectF(0.0F, 0.0F, IMAGE_WIDTH.toFloat(), IMAGE_HEIGHT.toFloat()), stroke)
            canvas.drawOval(RectF(6.0F, 6.0F, IMAGE_WIDTH.toFloat() - 6.0F, IMAGE_HEIGHT.toFloat() - 6.0F), background)
            background.isDither = true
            background.shader = g
            canvas.drawOval(RectF(12.0F, 12.0F, IMAGE_WIDTH.toFloat() - 12.0F, IMAGE_HEIGHT.toFloat() - 12.0F), background)

            val categoryLetter = category[0].toString()
            val letterWidth = foreground.measureText(categoryLetter)
            val letterFM = foreground.fontMetrics
            canvas.drawText(categoryLetter, (IMAGE_WIDTH - letterWidth) / 2, (IMAGE_HEIGHT - letterFM.top - letterFM.bottom) / 2 - 5, foreground)

            Log.d("getCategoryImage", "success")
            return bmp
        }
    }
}

