package ru.jdev.q5.gathering

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

private const val nbsp = 160.toChar()

class CheckPatternTest {

    @Test
    fun `CheckPattern should not fail when sumGroupIdx greater pattern's groups count`() {
        // Given check pattern with pattern without groups and sumGroupIdx = 2
        val pattern = createCheckPattern(".*", 2)

        // When it matches any string
        val check = pattern.tryParse("any")

        // Then result should be null
        assertThat(check, nullValue())
    }

    @Test
    fun `CheckPattern should not fail when placeGroupIdx greater pattern's groups count`() {
        // Given check pattern with pattern without groups and placeGroupIdx = 3
        val pattern = createCheckPattern("(.*)", 1, placeGroupIdx = 3)

        // When it matches any string
        val check = pattern.tryParse("any")

        // Then result should be null
        assertThat(check, nullValue())
    }

    @Test
    fun `CheckPattern should parse string with nbsp`() {
        // Given check pattern with pattern, that matches nbsp
        val pattern = createCheckPattern(".*(\\d\\p{Z}\\d+).*")

        // When it matched against sum with nbsp
        val check = pattern.tryParse("Test 1${nbsp}000")

        // Then it should successfully find sum
        assertThat(check?.sum, equalTo("1000"))
    }

}