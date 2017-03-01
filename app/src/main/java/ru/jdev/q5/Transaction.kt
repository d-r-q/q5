package ru.jdev.q5

import android.annotation.SuppressLint
import android.os.Build
import java.io.Serializable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class Transaction(val date: TrxDate, val sum: String, val category: String, val comment: String, val device: String, val source: String) : Serializable {

    constructor(sum: String, category: String, comment: String, source: String, datetime: TrxDate = TrxDate(Date())) : this(datetime, sum, category, comment, Build.DEVICE, source)

    fun toCsvLine() = "\"${date.date()}\",\"${date.time()}\",\"$sum\",\"$category\",\"${echoiedComment()}\",\"$device\",\"$source\""

    private fun echoiedComment() = comment.replace("\"", "\"\"")
}

@SuppressLint("SimpleDateFormat")
class TrxDate(private val date: Date) : Serializable {

    companion object {
        private val dateFormat = SimpleDateFormat("yy.MM.dd")
        private val timeFormat = SimpleDateFormat("HH:mm")
        private val dateTimeFormat = SimpleDateFormat("yy.MM.dd HH:mm")

        fun isValidDate(date: String) = dateFormat.matches(date)
        fun isValidTime(time: String) = timeFormat.matches(time)
    }

    constructor(date: String, time: String) : this(dateTimeFormat.parse("$date $time"))

    fun date(): String = dateFormat.format(date)
    fun time(): String = timeFormat.format(date)
}

fun SimpleDateFormat.matches(str: String) = try {
    this.parse(str)
    true
} catch (e: ParseException) {
    false
}