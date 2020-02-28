package ru.jdev.q5.gathering

const val split = "%@~"

val patterns = arrayOf(
        // Google Pay
        CheckPattern("(.*)$split([\\d,. ]*) .*с карты.*".toRegex(), 2, 1),
        // Kukuruza
        CheckPattern("Кукуруза$split-([\\d,. ]*) RUR\\s(.*.) Остаток.*".toRegex(), 1, 2),
        // Alfa
        CheckPattern("Уведомление$split.*Pokupka.*Summa: ([\\d,. ]*) .* RUR (.*) \\d{2}\\..*".toRegex(), 1, 2),
        CheckPattern(".*Списание.*сумму ([\\d,. ]+) .*;.*".toRegex(), 1, null),
        CheckPattern(".*Покупка совершена успешно\\. Сумма: ([\\d,.]+).*; (.*);.*".toRegex(), 1, 2),
        // Sber
        CheckPattern("(Перевод|Покупка) (.*)$split([\\d,. ]*) .*".toRegex(), 3, 2)
)

fun parseNotification(title: String, text: String): Check? {
    val ft = "$title$split$text"
    return patterns.map { it to it.pattern.matchEntire(ft) }
            .firstOrNull() { it.second != null }
            ?.let { (cp, mr) ->
                mr?.let {
                    val place = cp.placeGroupIdx
                            ?.let { idx -> it.groups[idx]!!.value }
                    Check(normalizeSum(it.groups[cp.sumGroupIdx]!!.value), place, "$title\n$text")
                }
            }
}

fun normalizeSum(sum: String): String {
    var res = sum.replace(" ", "")
    while ((res.indexOf(".") to res.indexOf(",")).let { (dot, comma) -> dot != -1 && comma != -1 && dot != comma } ||
            res.indexOf(".") != res.lastIndexOf(".") ||
            res.indexOf(",") != res.lastIndexOf(",")) {
        res = res.replaceFirst("[^\\d]".toRegex(), "")
    }
    return res
}

data class CheckPattern(val pattern: Regex, val sumGroupIdx: Int, val placeGroupIdx: Int?)