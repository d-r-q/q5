package ru.jdev.q5.gathering

import java.util.*

data class CheckPatternExamples(val pattern: CheckPattern, val examples: List<String>)

interface CheckPatterns {

    fun save(pattern: CheckPattern, examples: List<String>)

    fun listPatterns(): List<CheckPattern>

    fun load(id: UUID): CheckPatternExamples?

    fun delete(id: UUID)

}