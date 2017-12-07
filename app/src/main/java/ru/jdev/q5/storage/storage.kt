package ru.jdev.q5.storage

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
                FileInputStream(source).bufferedReader().lineSequence()
                        .withIndex()
                        .map(parse)
                        .toCollection(ArrayList<T>())
            } else {
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

    fun persist() {
        val content = elements.map { serialize }
                .joinToString("\n")
        BufferedWriter(FileWriter(source)).use {
            it.write(content)
        }
    }

}