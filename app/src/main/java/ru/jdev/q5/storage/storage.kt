package ru.jdev.q5.storage

import android.content.Context
import android.util.Log
import qbit.*
import qbit.ns.Namespace
import qbit.schema.RefAttr
import qbit.schema.ScalarAttr
import qbit.schema.eq
import qbit.storage.FileSystemStorage
import ru.jdev.q5.Categories
import ru.jdev.q5.TransactionLog
import ru.jdev.q5.storage.ECat.name
import ru.jdev.q5.storage.ETrx.category
import ru.jdev.q5.storage.ETrx.comment
import ru.jdev.q5.storage.ETrx.dateTime
import ru.jdev.q5.storage.ETrx.sum
import ru.jdev.q5.storage.ETrx.device
import ru.jdev.q5.storage.ETrx.source
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.time.ZoneId
import java.time.ZonedDateTime

interface Item {
    val id: Int?
}

object ETrx {
    private val trx = Namespace.of("q5", "transaction")
    val sum = ScalarAttr(trx["sum"], QLong)
    val dateTime = ScalarAttr(trx["dateTime"], QZonedDateTime)
    val category = RefAttr(trx["category"])
    val comment = ScalarAttr(trx["comment"], QString)
    val source = ScalarAttr(trx["source"], QString)
    val device = ScalarAttr(trx["device"], QString)
}

object ECat {

    private val cat = Namespace.of("q5", "category")

    val name = ScalarAttr(cat["name"], QString, true)

}

private var conn: LocalConn? = null

fun openConn(ctx: Context): qbit.LocalConn {
    var c = conn
    if (c != null) {
        return c
    }

    val dbDir = ctx.getExternalFilesDir(null)!!.toPath().resolve("db")
    c = qbit.qbit(FileSystemStorage(dbDir))
    conn = c
    ensureDbInitialized(c, ctx)
    ensureDbImported(c, ctx)

    return c
}

private fun ensureDbImported(conn: LocalConn, ctx: Context) {
    if (!conn.db.query(hasAttr(sum)).isEmpty()) {
        return
    }

    val categories = HashMap<String, Entity>()
    val importedCategories = ArrayList<Entity>()
    val es = ArrayList<Entity>()
    val log = TransactionLog(ctx)
    log.partNames().forEach { part ->
        log.part(part).list().forEach {
            var category = categories[it.category]
            if (category == null) {
                category = conn.db.query(attrIs(name, it.category)).firstOrNull()
                if (category != null) {
                    categories[it.category] = category
                }
            }
            if (category == null) {
                category = Entity(name eq it.category)
                categories[it.category] = category
                importedCategories.add(category)
            }

            val e = Entity(dateTime eq ZonedDateTime.ofInstant(it.date.dateTime.toInstant(), ZoneId.systemDefault()),
                    sum eq (it.sum.replace(",", ".").toDouble() * 100).toLong(),
                    ETrx.category eq category,
                    comment eq it.comment,
                    device eq it.device,
                    source eq it.source)
            es.add(e)
        }
    }

    val toStore = es + importedCategories
    conn.persist(toStore)
}

private fun ensureDbInitialized(conn: LocalConn, ctx: Context) {
    if (conn.db.query(attrIs(qbit.EAttr.name, "q5.category/name")).isEmpty()) {
        val schema = listOf(sum, dateTime, category, comment, source, device, name)
        conn.persist(schema)
    }

    if (conn.db.query(hasAttr(name)).isEmpty()) {
        val defaultCats = Categories(ctx).names().map { Entity(name eq it) }
        conn.persist(defaultCats)
    }
}

class QCollection<T : Item>(private val source: File, parse: (IndexedValue<String>) -> T, private val serialize: (T) -> String) {

    private val elements: ArrayList<T> =
            if (source.exists()) {
                Log.d("QCollection", "Load data from ${source.absolutePath}")
                FileInputStream(source).bufferedReader().lineSequence()
                        .withIndex()
                        .map { parse(it) }
                        .toCollection(ArrayList())
            } else {
                Log.d("QCollection", "No data file found, create parent dirs at ${source.parentFile.absolutePath}")
                source.parentFile.mkdirs()
                ArrayList()
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

}