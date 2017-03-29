package ru.jdev.q5

import android.annotation.SuppressLint
import android.os.Build
import ru.jdev.q5.R.string.*
import java.io.Serializable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class Transaction(val date: TrxDate, val sum: String, val category: String, val comment: String, val device: String, val source: String) : Serializable {

    companion object {
        private val delimiter = ';'
        fun parse(line: String): Transaction {

            val fields = line.split("\"$delimiter\"").map { it.trim('\"') }
            val date = if (fields.size > 0) fields[0] else "00.00.00"
            val time = if (fields.size > 1) fields[1] else "00:00"
            val sum = if (fields.size > 2) fields[2] else "not parsed"
            val cat = if (fields.size > 3) fields[3] else "not parsed"
            return try {
                Transaction(TrxDate(date, time), sum, cat, "TODO", "TODO", "TODO")
            } catch (e: ParseException) {
                Transaction(TrxDate("00.00.00", "00:00"), "0", "Not Parsed", "Not Parsed", "Not parsed", "NotParsed")
            }
        }
    }

    constructor(sum: String, category: String, comment: String, source: String, datetime: TrxDate = TrxDate(Date())) : this(datetime, sum, category, comment, Build.DEVICE, source)

    fun toCsvLine() = "\"${date.date()}\"$delimiter\"${date.time()}\"$delimiter\"${sum.replace('.', ',')}\"$delimiter\"$category\"$delimiter\"${echoiedComment()}\"$delimiter\"$device\"$delimiter\"$source\""

    private fun echoiedComment() = comment.replace("\"", "\"\"")
}

@SuppressLint("SimpleDateFormat")
class TrxDate(val dateTime: Date) : Serializable {

    companion object {
        val datePattern = "dd.MM.yyyy"
        val timePattern = "HH:mm"
        private val dateFormat = SimpleDateFormat(datePattern)
        private val timeFormat = SimpleDateFormat(timePattern)
        private val dateTimeFormat = SimpleDateFormat("$datePattern $timePattern")

        fun isValidDate(date: String) = dateFormat.matches(date)
        fun isValidTime(time: String) = timeFormat.matches(time)
    }

    constructor(date: String, time: String) : this(dateTimeFormat.parse("$date $time"))

    fun date(): String = dateFormat.format(dateTime)
    fun time(): String = timeFormat.format(dateTime)
}

fun SimpleDateFormat.matches(str: String) = try {
    this.parse(str)
    true
} catch (e: ParseException) {
    false
}