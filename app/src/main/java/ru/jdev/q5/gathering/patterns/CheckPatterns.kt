package ru.jdev.q5.gathering.patterns

import android.content.Context
import ru.jdev.q5.gathering.CheckPatterns
import java.io.File


object CheckPatternsModule {

    lateinit var checkPatterns: CheckPatterns

    fun init(ctx: Context) {
        checkPatterns = JsonCheckPatterns(File(ctx.getExternalFilesDir(null), "check_patterns.json"))
    }

}