package ru.jdev.q5

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.support.v4.app.NotificationCompat
import android.util.Log
import java.io.Serializable
import java.math.BigDecimal


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

                    val message = msg.displayMessageBody

                    Log.i("SmsReceiver", "senderNum: $phoneNumber; message: $message")

                    log.print("msg: $message")
                    val smsCheck = parseSms(message) ?: continue
                    log.print("msg is check: $smsCheck")
                    val sum = smsCheck.sum ?: continue
                    log.print("msg has sum")
                    val possibleCategory = categories.detectCategory(smsCheck)
                    log.print("Possible category: $possibleCategory")
                    val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    log.print("Notification manager: $mNotificationManager")
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
                        val nId = msg.indexOnIcc.let { if (it > -1) it else msg.timestampMillis.div(1000).toInt() }
                        setContentText(contentText)
                        val configIntent = Intent(context, TrxActivity::class.java)
                        configIntent.action = message
                        configIntent.putExtra("sum", sum)
                        configIntent.putExtra("comment", message)
                        configIntent.putExtra("smsCheck", smsCheck)
                        configIntent.putExtra(TrxActivity.sourceExtra, "sms")
                        if (possibleCategory != null) {
                            configIntent.putExtra("category", possibleCategory.eid.value())
                        }
                        val configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
                        setContentIntent(configPendingIntent)

                        if (possibleCategory != null) {
                            val saveIntent = Intent(context, FastSaveService::class.java)
                            saveIntent.action = message
                            saveIntent.putExtra("trx", QTransaction(BigDecimal(sum), possibleCategory, message, "sms").detouch())
                            saveIntent.putExtra("notificationId", nId)
                            val savePendingIntent = PendingIntent.getService(context, 0, saveIntent, 0)
                            addAction(android.support.v4.app.NotificationCompat.Action.Builder(android.R.drawable.ic_menu_save, "Сохранить", savePendingIntent).build())
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
            Log.e("SmsReceiver", "Exception smsReceiver $e")
            log.print(e.toString())
            e.printStackTrace()
        }

    }

}

fun parseSms(text: String): SmsCheck? {
    val parsers: List<(String) -> SmsCheck?> = listOf(
            ::parseAlfaBankSms,
            ::parseKukuruzaSms)
    return parsers.asSequence()
            .map { it(text) }
            .find { it != null }
}

private fun parseAlfaBankSms(text: String): AlfaBankSmsCheck? {
    // **3239 Pokupka Uspeshno Summa: 1 143 RUR Ostatok: 9 197,93 RUR RU/KOLTSOVO/TONUS 15.09.2018 08:47:30
    val match = """.*Summa: ((\d+ ?)+(,\d+)?) (RUR|EUR|USD).*(RUR|EUR|USD) (.*) [\d.: ]{19}""".toRegex().matchEntire(text)
    return if (match != null) {
        AlfaBankSmsCheck(match.groups[1]!!.value.replace(" ", ""), match.groups[6]!!.value)
    } else {
        null
    }
}

private fun parseKukuruzaSms(text: String): KukuruzaSmsCheck? {
    val parts = text.split(";").map(String::trim)
    Log.d("parseKukuruzaSms", parts.toString())
    if (parts.size < 2 || !parts[1].startsWith("Pokupka")) {
        return null
    }
    val match = """Pokupka: (\d+\.\d+)RUR.*""".toRegex().matchEntire(parts[1])
    Log.d("parseSms", match?.toString() ?: "not matched")
    val sum = match?.groups?.get(1)?.value
    sum ?: return null
    return KukuruzaSmsCheck(sum, parts[3])
}

abstract class SmsCheck(val sum: String?, val place: String?) : Serializable

class AlfaBankSmsCheck(sum: String?, place: String?) : SmsCheck(sum, place)

class KukuruzaSmsCheck(sum: String?, place: String?) : SmsCheck(sum, place)
