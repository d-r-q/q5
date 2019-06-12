package ru.jdev.q5

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import qbit.EID
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

const val DATE_FORMAT = "dd.MM.yyyy"
const val TIME_FORMAT = "HH:mm"

class TrxActivity : Activity() {

    private val trxLog = QTransactionLog(this)
    private lateinit var categories: Categories
    private lateinit var source: String
    private lateinit var trx: QTransaction<*>
    private var smsCheck: SmsCheck? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categories = Categories(this)

        val id = intent.getLongExtra(trxIdExtra, 0).takeIf { it != 0L }
        smsCheck = intent.getSerializableExtra("smsCheck") as SmsCheck?
        source = intent.getStringExtra(sourceExtra) ?: "unknown"
        val category = categories.byName(intent.getStringExtra("category") ?: "") ?: categories.categories().first()
        val sum = intent.getSerializableExtra("sum") as? String ?: "0"
        val comment = intent.getStringExtra("comment") ?: ""
        val dateTime = intent.getSerializableExtra("dateTime") as? ZonedDateTime ?: ZonedDateTime.now()

        trx = id?.let { trxLog.getTrx(EID(it)) } ?: QTransaction(BigDecimal(sum), category, comment, source, dateTime)

        Log.d("onCreate", smsCheck?.toString() ?: "null")

        setContentView(R.layout.activity_enter_sum)

        with(find<EditText>(R.id.sum_input)) {
            setText(trx.sum.toString())
        }
        with(find<Spinner>(R.id.category_input)) {
            val categories = categories.categories()
            val adapter = object : ArrayAdapter<QCategory<EID>>(context, android.R.layout.simple_spinner_item, categories) {

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    (view as TextView).text = getItem(position)?.name
                    return view
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    (view as TextView).text = getItem(position)?.name
                    return view
                }
            }
            println(trx.category)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.adapter = adapter
            this.setSelection(Math.max(categories.indexOfFirst { it.name == trx.category.name }, 0))
        }
        with(find<EditText>(R.id.comment_input)) {
            setText(trx.comment)
        }
        with(find<EditText>(R.id.date_input)) {
            setText(trx.dateTime.format(DateTimeFormatter.ofPattern(DATE_FORMAT)))
        }
        with(find<EditText>(R.id.time_input)) {
            setText(trx.dateTime.format(DateTimeFormatter.ofPattern(TIME_FORMAT)))
        }
        with(find<Button>(R.id.save_sum_button)) {
            setOnClickListener {
                if (onOk()) {
                    finish()
                }
            }
        }
    }

    private fun onOk(): Boolean {
        val sum = find<EditText>(R.id.sum_input).text.toString()
        val comment = find<EditText>(R.id.comment_input).text.toString()
        val category = find<Spinner>(R.id.category_input).selectedItem as QCategory<EID>
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
        val dateTime = LocalDateTime.parse(date + time, DateTimeFormatter.ofPattern("dd.MM.yyyyHH:mm"))
                .atZone(ZoneId.systemDefault())
        trx.apply {
            this.sum = BigDecimal(sum)
            this.comment = comment
            this.category = category
            this.dateTime = dateTime
        }

        if (!trxLog.storeTrx(trx)) {
            toast("Внешнее хранилище недоступно")
            return false
        }
        return true
    }

    companion object {
        internal const val sourceExtra = "source"
        internal const val trxIdExtra = "trxId"
    }

}

