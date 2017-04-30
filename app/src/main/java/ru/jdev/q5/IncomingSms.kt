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
                        val nId = msg.indexOnIcc.let { if (it > -1) it else msg.timestampMillis.div(1000).toInt() }
                        setContentText(contentText)
                        val configIntent = Intent(context, EnterSumActivity::class.java)
                        configIntent.action = message
                        configIntent.putExtra("sum", sum)
                        configIntent.putExtra("comment", message)
                        configIntent.putExtra("smsCheck", smsCheck)
                        configIntent.putExtra(EnterSumActivity.sourceExtra, "sms")
                        if (possibleCategory != null) {
                            configIntent.putExtra("category", possibleCategory)
                        }
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
        val parsers = listOf(
                this::parseAlfaBankSms,
                this::parseKukuruzaSms)
        return parsers.asSequence()
                .map { it(text) }
                .find { it != null }
    }

    private fun parseAlfaBankSms(text: String): AlfaBankSmsCheck? {
        val parts = text.split(";").map(String::trim)
        Log.d("parseAlfaBankSms", parts.toString())
        if (parts.size != 7 || parts[1] != "Pokupka") {
            return null
        }

        val match = """.*Summa: (\d+,\d+) RUR.*""".toRegex().matchEntire(parts[3])
        Log.d("parseSms", match?.toString() ?: "not matched")
        val sum = match?.groups?.get(1)?.value
        return AlfaBankSmsCheck(sum, parts[5])
    }

    private fun parseKukuruzaSms(text: String): KukuruzaSmsCheck? {
        // *7367; Pokupka: 189.00RUR; Ostatok: 1524.01RUR; UBER RU FEB21 EVNWM HE, help.uber.com; 20.02.2017 15:37; Tel 88007007710
        val parts = text.split(";").map(String::trim)
        Log.d("parseKukuruzaSms", parts.toString())
        if (parts.size != 6 || !parts[1].startsWith("Pokupka")) {
            return null
        }
        val match = """Pokupka: (\d+\.\d+)RUR.*""".toRegex().matchEntire(parts[1])
        Log.d("parseSms", match?.toString() ?: "not matched")
        val sum = match?.groups?.get(1)?.value
        return KukuruzaSmsCheck(sum, parts[3])
    }
}

abstract class SmsCheck(val sum: String?, val place: String?) : Serializable

class AlfaBankSmsCheck(sum: String?, place: String?) : SmsCheck(sum, place)

class KukuruzaSmsCheck(sum: String?, place: String?) : SmsCheck(sum, place)
