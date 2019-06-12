package ru.jdev.q5

import android.os.Build
import qbit.EID
import qbit.Entity
import qbit.mapping.AttrDelegate
import qbit.mapping.RefAttrDelegate
import qbit.mapping.TypedEntity
import qbit.schema.eq
import ru.jdev.q5.storage.Trxes
import ru.jdev.q5.storage.Trxes.device
import java.io.Serializable
import java.math.BigDecimal
import java.time.ZonedDateTime

fun QTransaction(sum: BigDecimal, category: QCategory<*>, comment: String, source: String, dateTime: ZonedDateTime = ZonedDateTime.now()) = QTransaction(
        Entity(
                Trxes.sum eq sum,
                Trxes.category eq category,
                Trxes.comment eq comment,
                Trxes.source eq source,
                Trxes.dateTime eq dateTime,
                device eq Build.DEVICE))

class QTransaction<E : EID?>(entity: Entity<E>) : TypedEntity<E>(entity) {

    companion object {

        const val notParsed = "not parsed"

        const val delimiter = ';'

    }

    var dateTime: ZonedDateTime by AttrDelegate(Trxes.dateTime)

    var sum: BigDecimal  by AttrDelegate(Trxes.sum)

    var category: QCategory<*> by RefAttrDelegate(Trxes.category)

    var source: String by AttrDelegate(Trxes.source)

    var comment: String by AttrDelegate(Trxes.comment)

    var device: String by AttrDelegate(Trxes.device)


}

