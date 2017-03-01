package ru.jdev.q5

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import java.util.*


class EnterSumActivity : Activity() {

    private val categories = Categories(this)
    lateinit var source: String
    var smsCheck: SmsCheck? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        smsCheck = intent.getSerializableExtra("smsCheck") as SmsCheck?
        source = intent.getStringExtra(sourceExtra) ?: "unknown"
        val category = intent.getStringExtra("category") ?: ""
        val sum = intent.getStringExtra("sum") ?: ""
        val comment = intent.getStringExtra("comment") ?: ""

        Log.d("onCreate", smsCheck?.toString() ?: "null")

        setContentView(R.layout.activity_enter_sum)

        with(find<EditText>(R.id.sum_input)) {
            setText(sum)
        }
        with(find<Spinner>(R.id.category_input)) {
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, categories.names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.adapter = adapter
            this.setSelection(Math.max(categories.names.indexOf(category), 0))
        }
        with(find<EditText>(R.id.comment_input)) {
            setText(comment)
        }
        with(find<Button>(R.id.save_sum_button)) {
            setOnClickListener {
                if (onOk()) {
                    finish()
                }
            }
        }
        val trxDate = TrxDate(Date())
        with(find<EditText>(R.id.date_input)) {
            setText(trxDate.date())
        }
        with(find<EditText>(R.id.time_input)) {
            setText(trxDate.time())
        }
    }

    fun onOk(): Boolean {
        val sum = find<EditText>(R.id.sum_input).text.toString()
        val comment = find<EditText>(R.id.comment_input).text.toString()
        val category = find<Spinner>(R.id.category_input).selectedItem.toString()
        val date = find<EditText>(R.id.date_input).text.toString()
        val time = find<EditText>(R.id.time_input).text.toString()
        if (smsCheck != null) {
            categories.categoryAssigned(smsCheck!!, category)
        }
        if (sum.isBlank()) {
            toast("Введите сумму")
            return false
        }
        if (!TrxDate.isValidDate(date)) {
            toast("Неверный формат даты (гг.мм.дд)")
            return false
        }
        if (!TrxDate.isValidTime(time)) {
            toast("Неверный формат времени (чч:мм)")
            return false
        }
        if (!TransactionLog.storeTrx(this, Transaction(sum, category, comment, source, TrxDate(date, time)))) {
            toast("Внешнее хранилище недоступно")
            return false
        }
        return true
    }

    companion object {
        internal val sourceExtra = "source"
    }

}

