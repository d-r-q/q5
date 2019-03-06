package ru.jdev.q5

import android.annotation.SuppressLint
import android.os.Build
import ru.jdev.q5.storage.Item
import java.io.Serializable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Arrays.asList

data class Transaction(override val id: Int?, val date: TrxDate, val sum: String, val category: String, val comment: String, val device: String, val source: String) : Serializable, Item {

    companion object {

        val notParsed = "not parsed"

        private val delimiter = ';'

        fun parse(sourceLine: IndexedValue<String>): Transaction {
            val line = sourceLine.value
            val fieldsV1 = line
                    .replace("\uFEFF", "") // удаление BOM-ов
                    .split("\",\"".toRegex())
                    .map { it.trim('\"') }
            val fieldsV2 = line
                    .replace("\uFEFF", "") // удаление BOM-ов
                    .split("\";\"".toRegex())
                    .map { it.trim('\"') }
            val v1 = fieldsV1.size > fieldsV2.size
            val fields = if (v1) fieldsV1 else fieldsV2

            val date = if (fields.size > 0) fields[0] else "00.00.00"
            val time = if (fields.size > 1) fields[1] else "00:00"
            val sum = if (fields.size > 2) fields[2] else notParsed
            val cat = if (fields.size > 3) fields[3] else notParsed
            val comment = if (fields.size > 4) fields[4].replace("\"\"", "\"") else notParsed
            val device = if (fields.size > 5) fields[5] else notParsed
            val source = if (fields.size > 6) fields[6] else notParsed
            return try {
                Transaction(sourceLine.index, if (v1) TrxDate.parseV1(date, time) else TrxDate(date, time), sum, cat, comment, device, source)
            } catch (e: ParseException) {
                Transaction(null, TrxDate("00.00.00", "00:00"), "0", notParsed, notParsed, notParsed, notParsed)
            }
        }
    }

    constructor(id: Int?, sum: String, category: String, comment: String, source: String, datetime: TrxDate = TrxDate(Date())) : this(id, datetime, sum, category, comment, Build.DEVICE, source)

    fun toCsvLine() = "\"${date.date()}\"$delimiter\"${date.time()}\"$delimiter\"${sum.replace('.', ',')}\"$delimiter\"$category\"$delimiter\"${echoiedComment()}\"$delimiter\"$device\"$delimiter\"$source\""
            .replace("\n", "")

    fun toExternalCsvLine() =
            asList(date.date(), date.time(), "-" + sum.replace('.', ','), category, echoiedComment(),
                    device, source,
                    "Расход", "Факт", date.dateTime.month + 1, 1900 + date.dateTime.year)
                    .joinToString(delimiter.toString()) {
                        "\"$it\""
                    }


    private fun echoiedComment() = comment.replace("\"", "\"\"")
}

@SuppressLint("SimpleDateFormat")
class TrxDate(val dateTime: Date) : Serializable {

    companion object {
        private const val datePatternV1 = "yy.MM.dd"

        private const val datePattern = "dd.MM.yyyy"
        private const val timePattern = "HH:mm"

        private val dateTimeFormatV1 = SimpleDateFormat("$datePatternV1 $timePattern")

        private val dateFormat = SimpleDateFormat(datePattern)
        private val timeFormat = SimpleDateFormat(timePattern)
        private val dateTimeFormat = SimpleDateFormat("$datePattern $timePattern")

        fun parseV1(date: String, time: String) = TrxDate(dateTimeFormatV1.parse("$date $time"))

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