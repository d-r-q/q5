package ru.jdev.q5.gathering.patterns

import org.json.JSONArray
import org.json.JSONObject
import ru.jdev.q5.gathering.CheckPattern
import ru.jdev.q5.gathering.CheckPatternExamples
import ru.jdev.q5.gathering.CheckPatterns
import java.io.File
import java.util.*


class JsonCheckPatterns private constructor(
        private val file: File,
        private val checkPatterns: HashMap<UUID, CheckPatternExamples>
) : CheckPatterns {

    constructor(file: File) : this(file, HashMap()) {
        val json = file.takeIf { it.exists() }?.readText()
        if (json?.isNotEmpty() != true) {
            return
        }

        checkPatterns +=
                JSONObject(json).keys().asSequence().map { key ->
                            val cpe = JSONObject(json).getJSONObject(key)
                            val checkPatternObj = cpe.getJSONObject("pattern")
                            val examples = parseExamples(cpe)
                            val checkPattern = parseCheckPattern(checkPatternObj)
                            CheckPatternExamples(checkPattern, examples)
                        }
                        .associateBy { it.pattern.id }
    }

    override fun save(pattern: CheckPattern, examples: List<String>) {
        checkPatterns[pattern.id] = CheckPatternExamples(pattern, examples)
        val json = checkPatterns.toJson().toString(2)
        file.writeText(json)
    }

    override fun listPatterns(): List<CheckPattern> =
            checkPatterns.values.map { it.pattern }

    override fun load(id: UUID): CheckPatternExamples? =
            checkPatterns[id]

    override fun delete(id: UUID) {
        checkPatterns.remove(id)
        val json = checkPatterns.toJson().toString(2)
        file.writeText(json)
    }

    private fun parseCheckPattern(checkPatternObj: JSONObject) =
            with(checkPatternObj) {
                CheckPattern(
                        UUID.fromString(getString("id")),
                        getString("name"),
                        getString("pattern").toRegex(),
                        getInt("sumGroupIdx"),
                        getString("placeGroupIdx").takeIf { it != "null" }?.toInt())
            }

    private fun parseExamples(cpe: JSONObject) =
            with(cpe.getJSONArray("examples")) {
                (0 until length()).map { idx ->
                    this.getString(idx)
                }
            }

    private fun HashMap<UUID, CheckPatternExamples>.toJson() =
            JSONObject(
                    this.map { e -> e.key.toString() to e.value.toJson() }
                            .toMap()
            )

    private fun CheckPatternExamples.toJson() =
            JSONObject(
                    mapOf("examples" to JSONArray(examples),
                            "pattern" to pattern.toJson())
            )

    private fun CheckPattern.toJson() =
            JSONObject(
                    mapOf(
                            "id" to id,
                            "name" to name,
                            "pattern" to pattern.toString(),
                            "sumGroupIdx" to sumGroupIdx.toString(),
                            "placeGroupIdx" to placeGroupIdx?.toString()
                    )
            )

}
