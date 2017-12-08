package ru.jdev.q5

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

private val UTF_8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

class TransactionLog(private val context: Context) {

    private val trxFileNameFormat = SimpleDateFormat("yyMM'-${Build.DEVICE}-v2.csv'")

    fun storeTrx(trx: Transaction): Boolean {
        if (!isExternalStorageWritable()) {
            return false
        }

        val file = file()
        Log.d("storeTrx", "${file.parentFile.exists()}")
        if (!file.parentFile.exists()) {
            Log.d("storeTrx", "Creating Q5 dir")
            file.parentFile.mkdirs()
        }
        BufferedWriter(OutputStreamWriter(FileOutputStream(file, true), "UTF-8")).use {
            if (file.length() > 0) {
                it.newLine()
            }
            Log.d("storeTrx", "Line: ${trx.toCsvLine()}")
            it.write(trx.toCsvLine())
            it.flush()
        }
        return true
    }

    fun parts(): List<LogPart> {
        Log.d("transactionLog", "parts")
        if (!(context.getExternalFilesDir(null)?.exists() ?: false)) {
            return listOf()
        }
        return context.getExternalFilesDir(null)
                .listFiles({ file -> file.name.endsWith(".csv") })
                .map(::LogPart)
    }

    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            return true
        }
        return false
    }

    private fun file() = File(context.getExternalFilesDir(null), trxFileNameFormat.format(Date()))
}

class LogPart(private val content: File) {

    val name: String = content.name

    fun list(): Sequence<Transaction> {
        val file = content
        if (!file.exists()) {
            return emptySequence()
        }
        return BufferedReader(InputStreamReader(FileInputStream(file), "UTF-8")).lineSequence()
                .map(String::trim)
                .filter { !it.isEmpty() }
                .map { Transaction.parse(it) }
    }

    fun sharableView(): ByteArray {
        val file = content
        Log.d("LogPart", "Sharing file: ${file.name}")
        if (!file.exists()) {
            return UTF_8_BOM
        }
        val out = ByteArrayOutputStream()
        val writer = BufferedWriter(OutputStreamWriter(out, "UTF-8"))
        out.write(UTF_8_BOM)
        BufferedReader(InputStreamReader(FileInputStream(file), "UTF-8")).lineSequence()
                .forEach { writer.write(it); writer.newLine()  }
        writer.flush()
        Log.d("LogPart", "Out: ${String(out.toByteArray())}")
        return out.toByteArray()
    }

    override fun toString(): String = name

}