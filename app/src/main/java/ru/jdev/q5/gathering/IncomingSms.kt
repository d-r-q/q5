package ru.jdev.q5.gathering

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.support.v4.app.NotificationCompat
import android.telephony.SmsMessage
import android.util.Log
import ru.jdev.q5.Categories
import ru.jdev.q5.EnterSumActivity
import ru.jdev.q5.R
import ru.jdev.q5.Transaction
import java.io.Serializable


class IncomingSms : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val checkNotifier = CheckNotifier(context)
        val bundle = intent.extras

        try {

            if (bundle != null) {

                val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (msg in msgs) {

                    val phoneNumber = msg.displayOriginatingAddress

                    val message = msg.displayMessageBody

                    Log.i("SmsReceiver", "senderNum: $phoneNumber; message: $message")

                    val check = parseSms(message) ?: continue
                    checkNotifier.handleCheck(check)
                }
            }
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Exception smsReceiver $e")
        }
    }
}

fun parseSms(text: String): Check? {
    val parsers: List<(String) -> Check?> = listOf(
            ::parseAlfaBankSms,
            ::parseKukuruzaSms)
    return parsers.asSequence()
            .map { it(text) }
            .find { it != null }
}

private fun parseAlfaBankSms(text: String): Check? {
    // **3239 Pokupka Uspeshno Summa: 1 143 RUR Ostatok: 9 197,93 RUR RU/KOLTSOVO/TONUS 15.09.2018 08:47:30
    val match = """.*Summa: ((\d+ ?)+(,\d+)?) (RUR|EUR|USD).*(RUR|EUR|USD) (.*) [\d.: ]{19}""".toRegex().matchEntire(text)
    return if (match != null) {
        Check(match.groups[1]!!.value.replace(" ", ""), match.groups[6]!!.value, text)
    } else {
        null
    }
}

private fun parseKukuruzaSms(text: String): Check? {
    val parts = text.split(";").map(String::trim)
    Log.d("parseKukuruzaSms", parts.toString())
    if (parts.size < 2 || !parts[1].startsWith("Pokupka")) {
        return null
    }
    val match = """Pokupka: (\d+\.\d+)RUR.*""".toRegex().matchEntire(parts[1])
    Log.d("parseSms", match?.toString() ?: "not matched")
    val sum = match?.groups?.get(1)?.value
    sum ?: return null
    return Check(sum, parts[3], text)
}

class Check(val sum: String?, val place: String?, val fullText: String) : Serializable

