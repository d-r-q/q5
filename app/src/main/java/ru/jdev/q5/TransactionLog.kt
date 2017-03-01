package ru.jdev.q5

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class TransactionLog(private val context: Context) {

    private val trxFileNameFormat = SimpleDateFormat("yyMM'-${Build.DEVICE}.csv'")

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

    fun list(): Sequence<Transaction> {
        val file = file()
        if (!file.exists()) {
            return emptySequence()
        }
        return BufferedReader(InputStreamReader(FileInputStream(file), "UTF-8")).lineSequence()
                .map { it.split("\",\"").map { it.trim('\"') } }
                .map {
                    Transaction(TrxDate(it[0], it[1]), it[2], it[3], "TODO", "TODO", "TODO")
                }
    }

    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            return true
        }
        return false
    }

    fun file() = File(context.getExternalFilesDir(null), trxFileNameFormat.format(Date()))
}