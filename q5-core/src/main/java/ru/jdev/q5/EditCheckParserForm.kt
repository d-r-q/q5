package ru.jdev.q5

import ru.jdev.q5.gathering.Check
import ru.jdev.q5.gathering.CheckPattern
import ru.jdev.q5.gathering.CheckPatterns
import java.util.*
import kotlin.collections.ArrayList

class EditCheckParserForm(private val checkPatterns: CheckPatterns) {

    var checkPattern = CheckPattern(UUID.randomUUID(), "Шаблон чека", "$.+^".toRegex(), 0, null)

    var checkExamples: MutableList<String> = ArrayList()

    fun load(id: UUID) {
        val cpe = checkPatterns.load(id) ?: return

        checkPattern = cpe.pattern
        checkExamples = ArrayList(cpe.examples)
    }

    fun onCheckPatternUpdated(checkPattern: CheckPattern) {
        this.checkPattern = checkPattern
    }

    fun onCheckExampleAdded(check: String) {
        checkExamples.add(check)
    }

    fun onCheckExampleUpdated(check: String, idx: Int) {
        checkExamples[idx] = check
    }

    fun onSave() {
        checkPatterns.save(checkPattern, checkExamples)
    }

    fun parseResults(): List<Check?> =
            checkExamples.map(checkPattern::tryParse)

}

