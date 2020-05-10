package ru.jdev.q5.storage

import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter

interface Item {
    val id: Int?
}

class QCollection<T : Item>(private val source: File, parse: (IndexedValue<String>) -> T, private val serialize: (T) -> String) {

    private val elements: ArrayList<T> =
            if (source.exists()) {
                Log.v("QCollection", "Load data from ${source.absolutePath}")
                FileInputStream(source).bufferedReader().lineSequence()
                        .withIndex()
                        .map { parse(it) }
                        .toCollection(ArrayList<T>())
            } else {
                Log.v("QCollection", "No data file found, create parent dirs at ${source.parentFile.absolutePath}")
                source.parentFile.mkdirs()
                ArrayList<T>()
            }

    fun list(): List<T> = elements

    fun with(item: T) {
        val id = item.id
        if (id == null) {
            elements.add(item)
        } else {
            elements[id] = item
        }
    }

    fun delete(id: Int): T =
            elements.removeAt(id)

    fun persist() {
        val content = elements.joinToString("\n") { serialize(it) }
        BufferedWriter(FileWriter(source)).use {
            it.write(content)
        }
    }

}