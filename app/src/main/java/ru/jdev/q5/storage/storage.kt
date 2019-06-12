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
import ru.jdev.q5.storage.Cats.name
import ru.jdev.q5.storage.Trxes.category
import ru.jdev.q5.storage.Trxes.comment
import ru.jdev.q5.storage.Trxes.dateTime
import ru.jdev.q5.storage.Trxes.device
import ru.jdev.q5.storage.Trxes.source
import ru.jdev.q5.storage.Trxes.sum
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal
import java.time.ZoneId
import java.time.ZonedDateTime

interface Item {
    val id: Long?
}

object Trxes {
    private val trx = Namespace.of("q5", "transaction")
    val sum = ScalarAttr(trx["sum"], QDecimal)
    val dateTime = ScalarAttr(trx["dateTime"], QZonedDateTime)
    val category = RefAttr(trx["category"])
    val comment = ScalarAttr(trx["comment"], QString)
    val source = ScalarAttr(trx["source"], QString)
    val device = ScalarAttr(trx["device"], QString)
}

object Cats {

    private val cat = Namespace.of("q5", "category")

    val name = ScalarAttr(cat["name"], QString, true)

}

private var conn: LocalConn? = null

fun openConn(ctx: Context): LocalConn {
    var c = conn
    if (c != null) {
        return c
    }

    val dbDir = File(ctx.getExternalFilesDir(null)!!, "db")
    c = qbit(FileSystemStorage(dbDir))
    conn = c
    ensureDbInitialized(c, ctx)
    ensureDbImported(c, ctx)

    return c
}

private fun ensureDbImported(conn: LocalConn, ctx: Context) {
    if (conn.db.query(hasAttr(sum)).firstOrNull() != null) {
        return
    }

    val categories = HashMap<String, Entity<*>>()
    val importedCategories = ArrayList<Entity<*>>()
    val es = ArrayList<Entity<*>>()
    @Suppress("DEPRECATION") val log = TransactionLog(ctx)
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

            val entries = sum eq BigDecimal(it.sum.replace(",", "."))
            val e = Entity(dateTime eq ZonedDateTime.ofInstant(it.date.dateTime.toInstant(), ZoneId.systemDefault()),
                    entries,
                    Trxes.category eq category,
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
    if (conn.db.query(attrIs(EAttr.name, "q5.category/name")).none()) {
        val schema = listOf(sum, dateTime, category, comment, source, device, name)
        conn.persist(schema)
    }

    if (conn.db.query(hasAttr(name)).none()) {
        val defaultCats = Categories(ctx).names().map { Entity(name eq it) }
        conn.persist(defaultCats)
    }
}

@Deprecated("Use qbit")
class QCollection<T : Item>(source: File, parse: (IndexedValue<String>) -> T) {

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
            elements[id.toInt()] = item
        }
    }

}