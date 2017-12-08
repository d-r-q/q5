package ru.jdev.q5

import android.content.Context
import android.util.Log
import ru.jdev.q5.storage.Item
import ru.jdev.q5.storage.QCollection
import java.io.File

data class Category(override val id: Int?, val name: String) : Item

class Categories(private val context: Context) {

    val categories = QCollection<Category>(File(context.getExternalFilesDir(null), "categories.txt"), { c -> Category(c.index, c.value) }, Category::name)

    init {
        if (categories.list().isEmpty()) {
            TransactionLog(context).parts().asSequence()
                    .flatMap { it.list() }
                    .map { Category(null, it.category) }
                    .distinct()
                    .forEach { categories.with(it) }
        }
    }

    fun categoryAssigned(smsCheck: SmsCheck, category: String) {
        with(context.getSharedPreferences("place2category", Context.MODE_PRIVATE).edit()) {
            putString(smsCheck.place, category)
            apply()
        }
        android.util.Log.d("onOk", "place2category item applied")
    }

    fun detectCategory(smsCheck: SmsCheck): String? = with(context.getSharedPreferences("place2category", Context.MODE_PRIVATE)) {
        for ((place, category) in all) {
            if (category is String && place == smsCheck.place) {
                return@with category
            }
        }
        null
    }

    fun names(): List<String> = categories.list().map { it.name }

}