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
            "Массаж",
            "Спорт",
            "Родственники",
            "Развлечения",
            "Медицина",
            "Хобби Лёши",
            "Транспорт",
            "Дом",
            "Хобби Марины",
            "Подарки",
            "Алкоголь",
            "Одежда",
            "Накопления/Инвестиции",
            "Отпуск",
            "Благотворительность",
            "Техника",
            "Комиссии",
            "Обучение/развитие",
            "Лёшины хотелки",
            "Маринины хотелки",
            "Прочее",
            "Сигареты",
            "Возврат долгов",
            "Бизнес/обязательное",
            "Бизнес/развитие",
            "Штрафы"
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