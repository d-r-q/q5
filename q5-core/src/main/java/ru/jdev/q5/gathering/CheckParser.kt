package ru.jdev.q5.gathering


class CheckParser {

    fun tryParse(patterns: List<CheckPattern>, checkCnd: String): Check? =
            patterns.mapNotNull { it.tryParse(checkCnd) }
                    .first()

}