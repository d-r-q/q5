package ru.jdev.q5.gathering

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import ru.jdev.q5.Categories
import ru.jdev.q5.EnterSumActivity
import ru.jdev.q5.R
import ru.jdev.q5.Transaction


class CheckNotifier(val context: Context) {

    private val categories = Categories(context)

    fun handleCheck(smsCheck: Check) {
        val sum = smsCheck.sum ?: return
        val possibleCategory = categories.detectCategory(smsCheck)
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel("q5-incoming-sms", "Обнаружена транзакция", NotificationManager.IMPORTANCE_DEFAULT)
            mNotificationManager.createNotificationChannel(mChannel)
        }

        with(NotificationCompat.Builder(context, "q5-incoming-sms")) {
            setSmallIcon(R.drawable.icon_transparent)
            setContentTitle("Обнаружена транзакция")
            val contentText = if (possibleCategory != null) {
                "$sum, $possibleCategory"
            } else {
                "Сумма: $sum"
            }
            val nId = (System.currentTimeMillis() and 0xFFFFFFFF).toInt()
            setContentText(contentText)
            val configIntent = Intent(context, EnterSumActivity::class.java)
            configIntent.action = smsCheck.fullText
            configIntent.putExtra("sum", sum)
            configIntent.putExtra("comment", smsCheck.fullText)
            configIntent.putExtra("smsCheck", smsCheck)
            configIntent.putExtra(EnterSumActivity.sourceExtra, "sms")
            if (possibleCategory != null) {
                configIntent.putExtra("category", possibleCategory)
            }
            val configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
            setContentIntent(configPendingIntent)

            if (possibleCategory != null) {
                val saveIntent = Intent(context, FastSaveService::class.java)
                saveIntent.action = smsCheck.fullText
                saveIntent.putExtra("trx", Transaction(null, sum, possibleCategory, smsCheck.fullText, "sms", logPart = null))
                saveIntent.putExtra("notificationId", nId)
                val savePendingIntent = PendingIntent.getService(context, 0, saveIntent, 0)
                addAction(NotificationCompat.Action.Builder(android.R.drawable.ic_menu_save, "Сохранить", savePendingIntent).build())
            }
            setAutoCancel(true)
            val res = build()
            mNotificationManager.notify(nId, res)
        }
    }

}