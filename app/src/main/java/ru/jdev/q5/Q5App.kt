package ru.jdev.q5

import android.app.Application
import ru.jdev.q5.gathering.patterns.CheckPatternsModule


class Q5App : Application() {

    override fun onCreate() {
        super.onCreate()
        CheckPatternsModule.init(this)
    }

}