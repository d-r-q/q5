package ru.jdev.q5

import android.content.Context
import android.os.Environment
import qbit.EID
import qbit.attrIn
import qbit.hasAttr
import qbit.mapping.pullAs
import qbit.mapping.queryAs
import ru.jdev.q5.storage.Trxes
import ru.jdev.q5.storage.openConn
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class QTransactionLog(private val context: Context) {

    fun storeTrx(trx: QTransaction<*>): Boolean {
        if (!isExternalStorageWritable()) {
            return false
        }

        val qBit = openConn(context)
        val (_, _) = qBit.persist(trx)

        return true
    }

    fun months(): List<LocalDate> {
        return openConn(context).db.query(hasAttr(Trxes.dateTime))
                .asSequence()
                .map { it[Trxes.dateTime].toLocalDate() }
                .distinctBy { it.year to it.month }
                .sortedDescending()
                .toList()
    }

    fun monthTrxs(date: LocalDate): List<QTransaction<EID>> {
        val qBit = openConn(context)
        val fromTime = ZonedDateTime.of(date.year, date.month.value, 1, 0, 0, 0, 0, ZoneId.systemDefault())
        val toTime = ZonedDateTime.of(date.year, date.month.value, 1, 0, 0, 0, 0, ZoneId.systemDefault()).plusMonths(1)
        return qBit.db.queryAs<QTransaction<EID>>(attrIn(Trxes.dateTime, fromTime, toTime)).toList()
    }


    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            return true
        }
        return false
    }

    fun getTrx(id: EID): QTransaction<EID>? =
            openConn(context).db.pullAs(id)

}
