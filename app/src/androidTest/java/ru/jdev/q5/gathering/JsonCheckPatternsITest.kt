package ru.jdev.q5.gathering

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.jdev.q5.gathering.patterns.JsonCheckPatterns
import java.io.File
import java.util.*


class JsonCheckPatternsITest {

    @Test
    fun testOpenAbsentFile() {
        // Given absent file
        val absent = File("/absent")

        // When repo is created
        val repo = JsonCheckPatterns(absent)

        // Then no exceptions are thrown and repo contains no patterns
        assertEquals(0, repo.listPatterns().size)
    }

    @Test
    fun testOpenEmptyFile() {
        // Given empty file
        val empty = File.createTempFile("q5-check-patterns-test", "json")

        // When repo is created
        val repo = JsonCheckPatterns(empty)

        // Then no exceptions are thrown and repo contains no patterns
        assertEquals(0, repo.listPatterns().size)
    }

    @Test
    fun testSaveCheckPattern() {
        // Given empty repo
        val dataFile = File.createTempFile("q5-check-patterns-test", "json")
        var repo = JsonCheckPatterns(dataFile)
        // and pattern with examples
        val aPattern = CheckPattern(UUID.randomUUID(), "Test checkPattern", ".*".toRegex(), 1, null)
        val examples = listOf("Example 1", "Example 2")

        // When check pattern is saved
        repo.save(aPattern, examples)
        // and repo is reopened
        repo = JsonCheckPatterns(dataFile)

        // Then the pattern can be loaded
        val checkPattern = repo.load(aPattern.id)
        // And it should be equal to saved
        assertEquals(aPattern, checkPattern?.pattern)
        // And it's examples should be equal to saved
        assertEquals(examples, checkPattern?.examples)
    }

}
