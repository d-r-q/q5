package ru.jdev.q5

import android.content.Context
import qbit.EID
import qbit.Entity
import qbit.attrIs
import qbit.hasAttr
import qbit.mapping.AttrDelegate
import qbit.mapping.TypedEntity
import qbit.mapping.queryAs
import qbit.schema.eq
import ru.jdev.q5.storage.Cats
import ru.jdev.q5.storage.Item
import ru.jdev.q5.storage.QCollection
import ru.jdev.q5.storage.openConn
import java.io.File

data class Category(override val id: Long?, val name: String) : Item

fun QCategory(name: String) = QCategory(Entity(Cats.name eq name))
class QCategory<E : EID?>(entity: Entity<E>) : TypedEntity<E>(entity) {

    var name: String by AttrDelegate(Cats.name)

}

private val DEFAULT_CATEGORIES = listOf("Продукты", "Рестораны", "Здоровье", "Машина")

class Categories(private val context: Context) {

    private val categories = QCollection(File(context.getExternalFilesDir(null), "categories.txt")) { c -> Category(c.index.toLong(), c.value) }
    private val qbit = openConn(context)

    init {
        if (categories.list().isEmpty()) {
            DEFAULT_CATEGORIES.forEach {
                categories.with(Category(null, it))
            }
        }
    }

    fun categoryAssigned(smsCheck: SmsCheck, category: QCategory<*>) {
        with(context.getSharedPreferences("place2category", Context.MODE_PRIVATE).edit()) {
            putString(smsCheck.place, category.name)
            apply()
        }
        android.util.Log.d("onOk", "place2category item applied")
    }

    fun detectCategory(smsCheck: SmsCheck): QCategory<EID>? = with(context.getSharedPreferences("place2category", Context.MODE_PRIVATE)) {
        val catName = this.getString(smsCheck.place, "") ?: ""
        return qbit.db.queryAs<QCategory<EID>>(attrIs(Cats.name, catName)).firstOrNull()
    }

    fun names(): List<String> = categories.list().map { it.name }

    fun categories(): List<QCategory<EID>> =
            qbit.db.queryAs<QCategory<EID>>(hasAttr(Cats.name))
                    .toList()

    fun byName(name: String): QCategory<EID>? =
            qbit.db.queryAs<QCategory<EID>>(attrIs(Cats.name, name))
                    .firstOrNull()

}