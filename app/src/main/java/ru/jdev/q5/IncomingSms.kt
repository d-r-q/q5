package ru.jdev.q5

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class IncomingSms : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras

        try {

            if (bundle != null) {


                val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (msg in msgs) {

                    val phoneNumber = msg.displayOriginatingAddress

                    val senderNum = phoneNumber
                    val message = msg.displayMessageBody

                    Log.i("SmsReceiver", "senderNum: $senderNum; message: $message")

                    val sum = parseSms(message) ?: continue
                    val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    with(Notification.Builder(context)){
                        setSmallIcon(R.drawable.coin)
                        setContentTitle("Обнаружена транзакция")
                        setContentText("Сумма: $sum")
                        val configIntent = Intent(context, EnterSumActivity::class.java)
                        configIntent.action = sum
                        configIntent.putExtra("sum", sum)
                        configIntent.putExtra("comment", message)
                        configIntent.putExtra("source", "sms")
                        val configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
                        setContentIntent(configPendingIntent)
                        setAutoCancel(true)
                        val res = build()
                        mNotificationManager.notify(0, res)
                    }

                } // end for loop
            } // bundle is null

        } catch (e: Exception) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e)
        }

    }

    fun parseSms(text: String): String? {
        val parts = text.split(";").map(String::trim)
        Log.d("parseSms", parts.toString())
        if (parts.size != 7 || parts[1] != "Pokupka") {
            return null
        }

        val match = """.*Summa: (\d+,\d+) RUR.*""".toRegex().matchEntire(parts[3])
        Log.d("parseSms", match?.toString() ?: "not matched")
        return match?.groups?.get(1)?.value
    }
}
