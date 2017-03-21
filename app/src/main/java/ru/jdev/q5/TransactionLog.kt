package ru.jdev.q5

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class TransactionLog(private val context: Context) {

    private val trxFileNameFormat = SimpleDateFormat("yyMM'-${Build.DEVICE}-v2.csv'")
    private val UTF_8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

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
        val fileOutputStream = FileOutputStream(file, true)
        fileOutputStream.write(UTF_8_BOM)
        BufferedWriter(OutputStreamWriter(fileOutputStream, "UTF-8")).use {
            if (file.length() > 0) {
                it.newLine()
            }
            Log.d("storeTrx", "Line: ${trx.toCsvLine()}")
            it.write(trx.toCsvLine())
            it.flush()
        }
        return true
    }

    fun list(): Sequence<Transaction> {
        val file = file()
        if (!file.exists()) {
            return emptySequence()
        }
        return BufferedReader(InputStreamReader(FileInputStream(file), "UTF-8")).lineSequence()
                .map { Transaction.parse(it) }
    }

    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            return true
        }
        return false
    }

    fun sharableView(): ByteArray {
        val file = file()
        if (!file.exists()) {
            return UTF_8_BOM
        }
        val out = ByteArrayOutputStream()
        out.write(UTF_8_BOM)
        BufferedReader(InputStreamReader(FileInputStream(file), "UTF-8")).lineSequence()
                .forEach { out.write(it.toByteArray()) }
        return out.toByteArray()
    }

    private fun file() = File(context.getExternalFilesDir(null), trxFileNameFormat.format(Date()))
}