package ru.jdev.q5.gathering

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test


class CheckParserTest {

    @Test
    fun `Test parsing of check matching second pattern`() {
        // Given list of two pattens and check matching second pattern
        val patterns = listOf(
                createCheckPattern(pattern = "\\w"),
                createCheckPattern(pattern = ".* (\\d+) .*")
        )
        val checkCnd = "abc 10 def"

        // When the check is parsed using patterns
        val check = CheckParser().tryParse(patterns, checkCnd)

        // Then it's parsed
        assertNotNull(check)
        // and it has correct sum
        assertEquals("10", check!!.sum)
    }

    @Test
    fun `Test parsing of check not matching any pattern`() {
        // Given list of two pattens and check matching second pattern
        val patterns = listOf(
                createCheckPattern(pattern = ".* (\\d+) .*")
        )
        val checkCnd = "abc def"

        // When the check is parsed using patterns
        val check = CheckParser().tryParse(patterns, checkCnd)

        // Then it's parsed
        assertNull(check)
    }

}