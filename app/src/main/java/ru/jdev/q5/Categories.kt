package ru.jdev.q5

import android.content.Context

class Categories(private val context: Context) {

    val names = arrayOf(
            "Продукты",
            "Машина",
            "Столовки/Бизнес-Ланчи",
            "Рестораны/Кафе/Бары",
            "Психотерапия",
            "Косметология",
            "Спорт",
            "Родственники",
            "Развлечения",
            "Медицина",
            "Хобби Лёши",
            "Транспорт",
            "Одежда",
            "Дом",
            "Хобби Марины",
            "Техника"
    )

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

}