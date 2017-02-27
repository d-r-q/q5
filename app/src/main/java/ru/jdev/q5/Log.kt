package ru.jdev.q5

import android.content.Context
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class Log(context: Context) {

    private val file = File(context.getExternalFilesDir(null), "log.txt")

    fun print(msg: String) {
        android.util.Log.d("q5.Log", msg)
        BufferedWriter(OutputStreamWriter(FileOutputStream(file, true), "UTF-8")).use {
            it.write(msg)
            it.newLine()
        }
    }
}