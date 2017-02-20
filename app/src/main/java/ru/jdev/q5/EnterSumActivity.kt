package ru.jdev.q5

import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ArrayAdapter
import android.widget.Spinner


class EnterSumActivity : Activity() {

    lateinit var category: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_sum)
        with(find<EditText>(R.id.sum_input)) {
            setText(intent.getStringExtra("sum") ?: "")
            setOnEditorActionListener { view, action, event ->
                if (action == EditorInfo.IME_ACTION_DONE) {
                    onOk()
                    true
                } else {
                    false
                }
            }
        }
        with(find<Button>(R.id.save_sum_button)) {
            setOnClickListener {
                onOk()
            }
        }

        with(find<EditText>(R.id.comment_input)) {
            setText(intent.getStringExtra("comment") ?: "")
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Categories.categories)
        with(find<Spinner>(R.id.category_input)) {
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.adapter = adapter
        }
        category = intent.getStringExtra("category") ?: ""
        with(find<Spinner>(R.id.category_input)) {
            this.setSelection(Math.max(Categories.categories.indexOf(category), 0))
        }
    }

    private val dateFormat = SimpleDateFormat("yy.MM.dd")
    private val timeFormat = SimpleDateFormat("HH:mm")

    fun onOk() {
        val sum = find<EditText>(R.id.sum_input).text.toString()
        val comment = find<EditText>(R.id.comment_input).text.toString()
        val category = find<Spinner>(R.id.category_input).selectedItem.toString()
        if (sum.isBlank()) {
            toast("Введите сумму")
            return
        }
        if (!storeTrx(sum, comment, category)) {
            toast("Внешнее хранилище недоступно")
            return
        }
        finish()
    }

    fun storeTrx(sum: String, comment: String, category: String): Boolean {
        Log.d("storeTrx", "$sum:$category")
        if (!isExternalStorageWritable()) {
            return false
        }

        val file = File(getExternalFilesDir(null), "1702-расходы.csv")
        Log.d("storeTrx", "${file.parentFile.exists()}")
        if (!file.parentFile.exists()) {
            Log.d("storeTrx", "Creating Q5 dir")
            file.parentFile.mkdirs()
        }
        val now = Date()
        val date = dateFormat.format(now)
        val time = timeFormat.format(now)
        val device = android.os.Build.DEVICE
        val source = intent.getStringExtra("source")
        BufferedWriter(OutputStreamWriter(FileOutputStream(file, true), "UTF-8")).use {
            if (file.length() > 0) {
                it.newLine()
            }
            it.write("\"$date\",\"$time\",\"$sum\",\"$category\",\"$comment\",\"$device\",\"$source\"")
            it.flush()
        }
        return true
    }

    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            return true
        }
        return false
    }

}
