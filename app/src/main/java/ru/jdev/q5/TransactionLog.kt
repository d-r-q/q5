package ru.jdev.q5

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import qbit.Entity
import qbit.attrIn
import qbit.attrIs
import ru.jdev.q5.Transaction.Companion.parse
import ru.jdev.q5.storage.*
import java.io.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

private val UTF_8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

class TransactionLog(private val context: Context) {

    private val trxFileNameFormat = SimpleDateFormat("yyMM'-${Build.DEVICE}-v2.csv'")


    fun part(logPart: String): LogPart = LogPart(File(context.getExternalFilesDir(null), logPart))

    fun partNames(): List<String> {
        Log.d("transactionLog", "partsFiles")
        if (!(context.getExternalFilesDir(null)?.exists() ?: false)) {
            return listOf()
        }
        return context.getExternalFilesDir(null)
                .listFiles({ file -> file.name.endsWith(".csv") })
                .map { it.name }
    }

    fun parts() = partNames()
            .map { part(it) }

}

class LogPart(private val content: File) {

    val name: String = content.name
    private val transactions = QCollection(content, { line -> parse(line) }, { it -> it.toCsvLine() })

    fun list(): List<Transaction> = transactions.list()

    fun sharableView(): ByteArray {
        val file = content
        Log.d("LogPart", "Sharing file: ${file.name}")
        if (!file.exists()) {
            return UTF_8_BOM
        }
        val out = ByteArrayOutputStream()
        val writer = BufferedWriter(OutputStreamWriter(out, "UTF-8"))
        out.write(UTF_8_BOM)
        list().forEach {
            writer.write(it.toExternalCsvLine()); writer.newLine()
        }
        writer.flush()
        Log.d("LogPart", "Out: ${String(out.toByteArray())}")
        return out.toByteArray()
    }

    override fun toString(): String = name

}