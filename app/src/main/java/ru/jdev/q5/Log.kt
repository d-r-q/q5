package ru.jdev.q5

import android.content.Context
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class Log(context: Context) {

    private val file by lazy { File(context.getExternalFilesDir(null), "log.txt") }
    private val dateFormat = SimpleDateFormat("yy.MM.dd HH:mm")

    fun print(msg: String) {
        android.util.Log.d("q5.Log", msg)
        BufferedWriter(OutputStreamWriter(FileOutputStream(file, true), "UTF-8")).use {
            it.write("${dateFormat.format(Date())}: $msg")
            it.newLine()
        }
    }
}