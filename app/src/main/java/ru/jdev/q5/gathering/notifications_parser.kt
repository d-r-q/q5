package ru.jdev.q5.gathering

import android.util.Log
import ru.jdev.q5.gathering.patterns.CheckPatternsModule
import ru.jdev.q5.logTag

val tag = logTag<CheckPattern>()

fun parseNotification(title: String, text: String): Check? {
    val ft = "$title\n$text"
    return CheckPatternsModule.checkPatterns.listPatterns().map {
                val res = it.tryParse(ft)
                Log.d(tag(), "Try to parse $ft with $it with result $res")
                res
            }
            .firstOrNull()
}
