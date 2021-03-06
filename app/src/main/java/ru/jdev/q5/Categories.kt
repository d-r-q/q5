package ru.jdev.q5

import android.content.Context
import ru.jdev.q5.gathering.Check
import ru.jdev.q5.storage.Item
import ru.jdev.q5.storage.QCollection
import java.io.File

data class Category(override val id: Int?, val name: String) : Item

private val DEFAULT_CATEGORIES = listOf("Продукты", "Рестораны", "Здоровье", "Машина")

class Categories(private val context: Context) {

    private val categories = QCollection(File(context.getExternalFilesDir(null), "categories.txt"), { c -> Category(c.index, c.value) }, Category::name)

    init {
        if (categories.list().isEmpty()) {
            TransactionLog(context).parts()
                    .flatMap { it.list() }
                    .groupBy { Category(null, it.category) }
                    .entries
                    .sortedByDescending { it.value.size }
                    .forEach { categories.with(it.key) }
        }
        if (categories.list().isEmpty()) {
            DEFAULT_CATEGORIES.forEach {
                categories.with(Category(null, it))
            }
        }
    }

    fun categoryAssigned(check: Check, category: String) {
        with(context.getSharedPreferences("place2category", Context.MODE_PRIVATE).edit()) {
            putString(check.place, category)
            apply()
        }
        android.util.Log.d("onOk", "place2category item applied")
    }

    fun detectCategory(check: Check): String? = with(context.getSharedPreferences("place2category", Context.MODE_PRIVATE)) {
        for ((place, category) in all) {
            if (category is String && place == check.place) {
                return@with category
            }
        }
        null
    }

    fun names(): List<String> = categories.list().map { it.name }

}