package ru.jdev.q5.gathering

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*


class CheckPatternTest {

    @Test
    fun `Test parsing of check matching second pattern`() {
        // Given list of two pattens and check matching second pattern
        val patterns = listOf(
                TestCheckPattern(withPattern = "\\w"),
                TestCheckPattern(withPattern = ".* (\\d+) .*")
        )
        val checkCnd = "abc 10 def"

        // When the check is parsed using patterns
        val check = CheckParser().tryParse(patterns, checkCnd)

        // Then it's parsed
        assertNotNull(check)
        // and it has correct sum
        assertEquals("10", check!!.sum)
    }

    private fun TestCheckPattern(withPattern: String) =
            CheckPattern(UUID.randomUUID(), "", withPattern.toRegex(), 1, 1)

}