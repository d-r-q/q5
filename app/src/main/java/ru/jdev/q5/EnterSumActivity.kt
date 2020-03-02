package ru.jdev.q5

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import ru.jdev.q5.gathering.Check
import java.util.*


class EnterSumActivity : Activity() {

    private val trxLog = TransactionLog(this)
    private lateinit var categories: Categories
    private lateinit var source: String
    private var check: Check? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categories = Categories(this)
        val trxDate = TrxDate(Date())

        val logPart = intent.getStringExtra(logPartExtra)
        val id = intent.getIntExtra(trxIdExtra, -1).takeIf { it != -1 }
        check = intent.getSerializableExtra("smsCheck") as Check?
        source = intent.getStringExtra(sourceExtra) ?: "unknown"
        val category = intent.getStringExtra("category") ?: ""
        val sum = intent.getStringExtra("sum") ?: ""
        val comment = intent.getStringExtra("comment") ?: ""
        val date = intent.getStringExtra("date") ?: trxDate.date()
        val time = intent.getStringExtra("time") ?: trxDate.time()

        Log.d("onCreate", check?.toString() ?: "null")

        setContentView(R.layout.activity_enter_sum)

        with(find<EditText>(R.id.sum_input)) {
            setText(sum)
        }
        with(find<Spinner>(R.id.category_input)) {
            val categoryNames = categories.names()
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, categoryNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.adapter = adapter
            this.setSelection(Math.max(categoryNames.indexOf(category), 0))
        }
        with(find<EditText>(R.id.comment_input)) {
            setText(comment)
        }
        with(find<EditText>(R.id.date_input)) {
            setText(date)
        }
        with(find<EditText>(R.id.time_input)) {
            setText(time)
        }
        with(find<Button>(R.id.save_sum_button)) {
            setOnClickListener {
                if (onOk(logPart, id)) {
                    finish()
                }
            }
        }
        with(find<Button>(R.id.delete_trx_button)) {
            visibility = if (id != null && id >= 0) {
                VISIBLE
            } else {
                GONE
            }
            setOnClickListener {
                val builder = AlertDialog.Builder(this@EnterSumActivity)
                builder.setTitle("Подтвердите удаление")
                builder.setMessage("Удалить транзакцию:\n $sum - $category")
                builder.setPositiveButton("Да") { _, _ ->
                    if (onDelete(logPart, id!!)) {
                        finish()
                    } else {
                        toast("Ошибка удаления")
                    }
                }
                builder.setNegativeButton("Нет") { _, _ -> /* noop */}
                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    private fun onOk(logPart: String?, id: Int?): Boolean {
        val sum = find<EditText>(R.id.sum_input).text.toString()
        val comment = find<EditText>(R.id.comment_input).text.toString()
        val category = find<Spinner>(R.id.category_input).selectedItem.toString()
        val date = find<EditText>(R.id.date_input).text.toString()
        val time = find<EditText>(R.id.time_input).text.toString()
        if (check != null) {
            categories.categoryAssigned(check!!, category)
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
        if (!trxLog.storeTrx(Transaction(id, sum, category, comment, source, TrxDate(date, time), logPart))) {
            toast("Внешнее хранилище недоступно")
            return false
        }
        return true
    }

    private fun onDelete(logPart: String, id: Int): Boolean {
        if (!trxLog.deleteTrx(logPart, id)) {
            toast("Ошибка удаления")
            return false
        }
        return true
    }

    companion object {
        internal val sourceExtra = "source"
        internal val logPartExtra = "logPart"
        internal val trxIdExtra = "trxId"
    }

}

