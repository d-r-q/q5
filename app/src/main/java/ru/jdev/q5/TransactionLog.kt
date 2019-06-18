package ru.jdev.q5

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import ru.jdev.q5.Transaction.Companion.parse
import ru.jdev.q5.storage.QCollection
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

val trxFileNameFormat = SimpleDateFormat("yyMM'-${Build.DEVICE}-v2.csv'")

class TransactionLog(private val context: Context) {

    fun storeTrx(trx: Transaction): Boolean {
        if (!isExternalStorageWritable()) {
            return false
        }

        val file = File(context.getExternalFilesDir(null), trx.logPart ?: trxFileNameFormat.format(trx.date.dateTime))
        Log.d("storeTrx", "${file.parentFile.exists()}")
        if (!file.parentFile.exists()) {
            Log.d("storeTrx", "Creating Q5 dir")
            file.parentFile.mkdirs()
        }
        LogPart(file).with(trx)
        return true
    }

    fun deleteTrx(logPart: String, id: Int): Boolean {
        if (!isExternalStorageWritable()) {
            return false
        }

        val file = File(context.getExternalFilesDir(null), logPart)
        Log.d("deleteTrx", "${file.parentFile.exists()}")
        if (!file.parentFile.exists()) {
            Log.d("deleteTrx", "Creating Q5 dir")
            file.parentFile.mkdirs()
        }
        LogPart(file).delete(id)
        return true
    }

    private fun part(logPart: String): LogPart = LogPart(File(context.getExternalFilesDir(null), logPart))

    private fun partNames(): List<String> {
        Log.d("transactionLog", "partsFiles")
        if (context.getExternalFilesDir(null)?.exists() != true) {
            return listOf()
        }
        return context.getExternalFilesDir(null)
                .listFiles { file -> file.name.endsWith(".csv") }
                .map { it.name }
    }

    fun parts() = partNames()
            .map { part(it) }

    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            return true
        }
        return false
    }

    fun find(fromDate: Date, toDate: Date): List<Transaction> {
        val res = ArrayList<Transaction>()
        partNames()
                .map { part(it) }
                .map { part ->
                    res.addAll(part.list().filter { it.date.dateTime in fromDate..toDate })
                }
        return res
    }

}

class LogPart(content: File) {

    val name: String = content.name
    private val transactions = QCollection(content, { line -> parse(name, line) }, { it.toCsvLine() })

    fun list(): List<Transaction> = transactions.list()

    fun with(trx: Transaction): LogPart {
        transactions.with(trx)
        transactions.persist()
        return this
    }

    fun delete(id: Int): LogPart {
        transactions.delete(id)
        transactions.persist()
        return this
    }

    override fun toString(): String = name

}