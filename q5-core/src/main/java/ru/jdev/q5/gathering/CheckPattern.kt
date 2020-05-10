package ru.jdev.q5.gathering

import java.util.*


data class CheckPattern(val id: UUID, val name: String, val pattern: Regex, val sumGroupIdx: Int, val placeGroupIdx: Int?) {

    fun tryParse(check: String): Check? {
        val result = pattern.matchEntire(check) ?: return null
        val sum = result.groupValues.elementAtOrNull(sumGroupIdx)
                ?.takeIf { it.matches("[\\d., ]+".toRegex()) }
                ?: return null
        val place = placeGroupIdx?.let { idx -> result.groups[idx]!!.value }?.takeIf { it.isNotBlank() }
        return Check(normalizeSum(sum), place, check)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CheckPattern

        if (id != other.id) return false
        if (name != other.name) return false
        if (pattern.pattern != other.pattern.pattern) return false
        if (sumGroupIdx != other.sumGroupIdx) return false
        if (placeGroupIdx != other.placeGroupIdx) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + pattern.pattern.hashCode()
        result = 31 * result + sumGroupIdx
        result = 31 * result + (placeGroupIdx ?: 0)
        return result
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
