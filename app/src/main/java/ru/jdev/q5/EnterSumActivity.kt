package ru.jdev.q5

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import org.jetbrains.anko.find
import org.jetbrains.anko.toast


class EnterSumActivity : Activity() {

    lateinit var source: String
    var smsCheck: SmsCheck? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        smsCheck = intent.getSerializableExtra("smsCheck") as SmsCheck?
        Log.d("onCreate", smsCheck?.toString() ?: "null")

        setContentView(R.layout.activity_enter_sum)

        with(find<EditText>(R.id.sum_input)) {
            setText(intent.getStringExtra("sum") ?: "")
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
        val category = intent.getStringExtra("category") ?: ""
        with(find<Spinner>(R.id.category_input)) {
            this.setSelection(Math.max(Categories.categories.indexOf(category), 0))
        }
        source = intent.getStringExtra(sourceExtra) ?: "unknown"
    }

    fun onOk() {
        val sum = find<EditText>(R.id.sum_input).text.toString()
        val comment = find<EditText>(R.id.comment_input).text.toString()
        val category = find<Spinner>(R.id.category_input).selectedItem.toString()
        if (smsCheck != null) {
            with(getSharedPreferences("place2category", Context.MODE_PRIVATE).edit()) {
                putString(smsCheck!!.place, category)
                apply()
            }
            Log.d("onOk", "place2category item applied")
        }
        if (sum.isBlank()) {
            toast("Введите сумму")
            return
        }
        if (!TransactionLog.storeTrx(this, Transaction(sum, category, comment, source))) {
            toast("Внешнее хранилище недоступно")
            return
        }
        finish()
    }

    companion object {
        internal val sourceExtra = "source"
    }

}

