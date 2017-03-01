package ru.jdev.q5

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import java.io.Serializable

class IncomingSms : BroadcastReceiver() {

    companion object {
        var id = 0
    }

    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras

        val log = Log(context)
        val categories = Categories(context)
        log.print("Incoming sms received")
        try {

            if (bundle != null) {

                log.print("bundle is not null")
                val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                log.print("msgs.len: ${msgs.size}")
                for (msg in msgs) {

                    val phoneNumber = msg.displayOriginatingAddress

                    val senderNum = phoneNumber
                    val message = msg.displayMessageBody

                    Log.i("SmsReceiver", "senderNum: $senderNum; message: $message")

                    log.print("msg: $message")
                    val smsCheck = parseSms(message) ?: continue
                    log.print("msg is check: $smsCheck")
                    val sum = smsCheck.sum ?: continue
                    log.print("msg has sum")
                    val possibleCategory = categories.detectCategory(smsCheck)
                    log.print("Possible category: $possibleCategory")
                    val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    log.print("Notification manager: $mNotificationManager")
                    with(Notification.Builder(context)) {
                        setSmallIcon(R.drawable.icon_transparent)
                        setContentTitle("Обнаружена транзакция")
                        val contentText = if (possibleCategory != null) {
                            "$sum, $possibleCategory"
                        } else {
                            "Сумма: $sum"
                        }
                        val nId = id++
                        setContentText(contentText)
                        val configIntent = Intent(context, EnterSumActivity::class.java)
                        configIntent.action = message
                        configIntent.putExtra("sum", sum)
                        configIntent.putExtra("comment", message)
                        configIntent.putExtra("smsCheck", smsCheck)
                        configIntent.putExtra(EnterSumActivity.sourceExtra, "sms")
                        val configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
                        setContentIntent(configPendingIntent)

                        if (possibleCategory != null) {
                            val saveIntent = Intent(context, FastSaveService::class.java)
                            saveIntent.action = message
                            saveIntent.putExtra("trx", Transaction(sum, possibleCategory, message, "sms"))
                            saveIntent.putExtra("notificationId", nId)
                            val savePendingIntent = PendingIntent.getService(context, 0, saveIntent, 0)
                            addAction(Notification.Action.Builder(android.R.drawable.ic_menu_save, "Сохранить", savePendingIntent).build())
                        }
                        setAutoCancel(true)
                        val res = build()
                        log.print("Before notify $nId $res")
                        mNotificationManager.notify(nId, res)
                        log.print("After notify")
                    }

                } // end for loop
            } // bundle is null

        } catch (e: Exception) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e)
            log.print(e.toString())
        }

    }

    fun parseSms(text: String): SmsCheck? {
        val parts = text.split(";").map(String::trim)
        Log.d("parseSms", parts.toString())
        if (parts.size != 7 || parts[1] != "Pokupka") {
            return null
        }

        val match = """.*Summa: (\d+,\d+) RUR.*""".toRegex().matchEntire(parts[3])
        Log.d("parseSms", match?.toString() ?: "not matched")
        val sum = match?.groups?.get(1)?.value
        return SmsCheck(parts[0], parts[1], parts[2], sum, parts[4], parts[5], parts[6])
    }
}

data class SmsCheck(val account: String?, val action: String?, val result: String?, val sum: String?, val rem: String?, val place: String?, val dateTime: String?) : Serializable