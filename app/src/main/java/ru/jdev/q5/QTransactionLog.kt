package ru.jdev.q5

import android.content.Context
import android.os.Environment
import qbit.*
import qbit.schema.eq
import ru.jdev.q5.storage.ECat
import ru.jdev.q5.storage.ETrx
import ru.jdev.q5.storage.openConn
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class QTransactionLog(private val context: Context) {

    fun storeTrx(trx: Transaction): Boolean {
        if (!isExternalStorageWritable()) {
            return false
        }

        val qBit = openConn(context)
        var cat = qBit.db.query(attrIs(ECat.name, trx.category)).firstOrNull()
        if (cat == null) {
            cat = qBit.persist(Entity(ECat.name eq trx.category)).storedEntity()
        }
        val e = qbit.Entity(ETrx.dateTime eq trx.date.zonedDateTime(),
                ETrx.sum eq (trx.sum.replace(",", ".").toDouble() * 100).toLong(),
                ETrx.category eq cat,
                ETrx.comment eq trx.comment,
                ETrx.device eq trx.device,
                ETrx.source eq trx.source)
        val (_, _) = qBit.persist(e)

        qBit.db.query(hasAttr(ETrx.category))
                .groupBy { it[ETrx.category] }
                .mapValues { (_, value) -> value.size }
                .maxBy { it.value }
        return true
    }

    fun months(): List<LocalDate> {
        return openConn(context).db.query(hasAttr(ETrx.dateTime))
                .asSequence()
                .map { it[ETrx.dateTime].toLocalDate() }
                .distinctBy { it.year to it.month }
                .sortedDescending()
               .toList()
    }

    fun monthTrxs(date: LocalDate): List<StoredEntity> {
        val qBit = openConn(context)
        val fromTime = ZonedDateTime.of(date.year, date.month.value, 1, 0, 0, 0, 0, ZoneId.systemDefault())
        val toTime = ZonedDateTime.of(date.year, date.month.value, 1, 0, 0, 0, 0,ZoneId.systemDefault()).plusMonths(1)
        return qBit.db.query(attrIn(ETrx.dateTime, fromTime, toTime))
    }


    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            return true
        }
        return false
    }

}
