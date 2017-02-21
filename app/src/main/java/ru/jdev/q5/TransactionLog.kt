package ru.jdev.q5

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

object TransactionLog {

    private val trxFileNameFormat = SimpleDateFormat("yyMM'-расходы.csv'")

    fun storeTrx(context: Context, trx: Transaction): Boolean {
        if (!isExternalStorageWritable()) {
            return false
        }

        val file = File(context.getExternalFilesDir(null), trxFileNameFormat.format(Date()))
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

    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            return true
        }
        return false
    }

}