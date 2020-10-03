package ru.jdev.q5.gathering

import java.util.*


fun createCheckPattern(pattern: String, sumGroupIdx: Int = 1, placeGroupIdx: Int = 1) =
        CheckPattern(UUID.randomUUID(), "", pattern.toRegex(), sumGroupIdx, placeGroupIdx)

