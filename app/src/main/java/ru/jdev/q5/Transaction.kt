package ru.jdev.q5

import android.annotation.SuppressLint
import android.os.Build
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class Transaction(val date: TrxDate, val sum: String, val category: String, val comment: String, val device: String, val source: String) : Serializable {

    constructor(sum: String, category: String, comment: String, source: String) : this(TrxDate(Date()), sum, category, comment, Build.DEVICE, source)

    fun toCsvLine() = "\"${date.date()}\",\"${date.time()}\",\"$sum\",\"$category\",\"${echoiedComment()}\",\"$device\",\"$source\""

    private fun echoiedComment() = comment.replace("\"", "\"\"")
}

@SuppressLint("SimpleDateFormat")
class TrxDate(private val date: Date) : Serializable {

    private val dateFormat = SimpleDateFormat("yy.MM.dd")
    private val timeFormat = SimpleDateFormat("HH:mm")

    fun date(): String = dateFormat.format(date)
    fun time(): String = timeFormat.format(date)
}