package ru.jdev.q5.gathering.patterns

import android.os.Bundle
import android.text.Editable
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import kotlinx.android.synthetic.main.edit_check_pattern_main.*
import ru.jdev.q5.EditCheckParserForm
import ru.jdev.q5.R
import ru.jdev.q5.gathering.CheckPattern
import java.util.*
import java.util.regex.PatternSyntaxException

class EditCheckPatternActivity : AppCompatActivity() {

    private lateinit var form: EditCheckParserForm

    private val checkFragment: CheckFragment
        get() = supportFragmentManager.findFragmentById(R.id.checkFragment) as CheckFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        form = EditCheckParserForm(CheckPatternsModule.checkPatterns)
        intent.getSerializableExtra("id")?.let {
            form.load(it as UUID)
        }
        supportFragmentManager.fragmentFactory = CheckFragmentFactory(form)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_check_pattern_main)

        fab.setOnClickListener {
            form.onSave()
            finish()
        }
        with(form.checkPattern) {
            checkPatternNameText.setText(name)
            checkPatternNameText.addTextChangedListener(afterTextChanged = ::updateCheckPattern)

            checkPatternText.addTextChangedListener(afterTextChanged = ::updateCheckPattern)
            checkPatternText.setText(pattern.toString())

            sumGroupText.addTextChangedListener(afterTextChanged = ::updateCheckPattern)
            sumGroupText.setText(sumGroupIdx.toString())

            placeGroupText.addTextChangedListener(afterTextChanged = ::updateCheckPattern)
            placeGroupText.setText(placeGroupIdx?.toString() ?: "")
        }

        addCheckExampleButton.setOnClickListener {
            form.onCheckExampleAdded("")
            onCheckExampleAdded()
        }
    }

    private fun onCheckExampleAdded() {
        checkFragment.listView.adapter?.notifyDataSetChanged()
    }

    private fun updateCheckPattern(any: Editable?) {
        val pattern = checkPatternText.str().toRegexOrNull() ?: return
        val sumGroupIdx = sumGroupText.str().takeIf { it.isNotEmpty() }?.toInt()
        if (sumGroupIdx != null) {
            val checkPattern = CheckPattern(form.checkPattern.id, checkPatternNameText.str(), pattern, sumGroupIdx, placeGroupText.str().toIntOrNull())
            form.onCheckPatternUpdated(checkPattern)
            checkFragment.listView.adapter?.notifyDataSetChanged()
        }
    }

}

class CheckFragmentFactory(private val form: EditCheckParserForm) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment =
            CheckFragment(form)

}

fun EditText.str() = this.text.toString()

fun String.toRegexOrNull() =
        try {
            this.toRegex()
        } catch (e: PatternSyntaxException) {
            null
        }