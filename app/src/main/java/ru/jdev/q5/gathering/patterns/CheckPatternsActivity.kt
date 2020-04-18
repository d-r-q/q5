package ru.jdev.q5.gathering.patterns

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_check_patterns.*
import org.jetbrains.anko.onClick
import ru.jdev.q5.R

class CheckPatternsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_patterns)
        with(patternsRecycler) {
            layoutManager = LinearLayoutManager(context)
            adapter = CheckPatternRecyclerViewAdapter(CheckPatternsModule.checkPatterns)
        }

        floatingActionButton.onClick {
            startActivity(Intent(this, EditCheckPatternActivity::class.java))
        }
    }

}