package ru.jdev.q5.gathering

import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


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

}