package ru.jdev.q5

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import org.jetbrains.anko.find
import org.jetbrains.anko.onClick
import org.jetbrains.anko.onItemSelectedListener
import ru.jdev.q5.QTransaction.Companion.delimiter
import ru.jdev.q5.TrxActivity.Companion.trxIdExtra
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.Arrays.asList


class LogActivity : AppCompatActivity() {

    private val qLog = QTransactionLog(this)
    private val tableParams = FrameLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val table = find<TableLayout>(R.id.table)
        table.layoutParams = tableParams
        table.setColumnShrinkable(0, true)
        table.setColumnShrinkable(1, true)
        table.setColumnShrinkable(2, true)
        table.setColumnStretchable(2, true)
        updateTable()

        find<FloatingActionButton>(R.id.fab).onClick {
            val configIntent = Intent(this, TrxActivity::class.java)
            configIntent.putExtra(TrxActivity.sourceExtra, "manual")
            startActivity(configIntent)
        }
        with(find<Spinner>(R.id.log_part)) {
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, qLog.months())
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.adapter = adapter
            this.setSelection(0)
            this.onItemSelectedListener { onItemSelected { _, _, _, _ -> updateTable() } }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_log, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.share) {
            return share()
        } else if (item?.itemId == R.id.sort) {
            return sort()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun share(): Boolean {
        val month = find<Spinner>(R.id.log_part).selectedItem as LocalDate? ?: LocalDate.now()
        val trxes = qLog.monthTrxs(month!!)

        val outputFile = File(applicationContext.externalCacheDir, "${Build.DEVICE}$month.csv")
        val datePattern = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val timePattern = DateTimeFormatter.ofPattern("HH:mm")
        BufferedWriter(FileWriter(outputFile)).use {
            trxes.forEach { trx ->
                val fields = asList(trx.dateTime.format(datePattern), trx.dateTime.format(timePattern),
                        "-" + trx.sum, trx.category.name, trx.comment.replace("\"", "\"\""),
                        trx.device, trx.source,
                        "Расход", "Факт", trx.dateTime.month.value, trx.dateTime.year)
                val line = fields.joinToString(delimiter.toString()) { value ->
                    "\"$value\""
                }

                it.write(line)
                it.newLine()
            }
            it.flush()
        }

        val path = FileProvider.getUriForFile(this, "ru.jdev.q5.fileprovider", outputFile)
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.type = "text/csv"
        sendIntent.putExtra(Intent.EXTRA_STREAM, path)
        startActivity(Intent.createChooser(sendIntent, "Отправить журнал"))

        return true
    }

    private fun sort(): Boolean {
        trxComparator = if (trxComparator == trxDateComparator) {
            trxSumComparator
        } else {
            trxDateComparator
        }
        updateTable()
        return true
    }

    private val trxDateComparator = Comparator<QTransaction<*>> { e1, e2 -> e1.dateTime.compareTo(e2.dateTime) }
    private val trxSumComparator = Comparator<QTransaction<*>> { e1, e2 -> e1.sum.compareTo(e2.sum) }
    private var trxComparator = trxDateComparator

    private fun updateTable() {
        val table = find<TableLayout>(R.id.table)

        table.removeAllViews()
        val month = find<Spinner>(R.id.log_part).selectedItem as LocalDate? ?: LocalDate.now()
        val trxes = qLog.monthTrxs(month!!).sortedByDescending { it.dateTime }
        val datePattern = DateTimeFormatter.ofPattern("dd/MMM")
        trxes.forEach { trx ->
            val row = createRow(trx.dateTime.format(datePattern), trx.sum.toString(), trx.category.name)
            row.onClick {
                val trxFormIntent = Intent(this, TrxActivity::class.java)
                trxFormIntent.putExtra(trxIdExtra, trx.eid.value())
                startActivity(trxFormIntent)
            }
            table.addView(row)
        }

        // todo: val fmt = NumberFormat.getCurrencyInstance()
        val row = createRow("", trxes.sumBy { it.sum.toInt() }.toString(), "Итого")
        table.addView(row)
    }

    private fun createRow(date: String, sum: String, category: String): TableRow {
        val dateLayoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
        dateLayoutParams.rightMargin = 40
        dateLayoutParams.topMargin = 40
        dateLayoutParams.bottomMargin = 40
        dateLayoutParams.weight = 1.0F

        val sumLayoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
        sumLayoutParams.gravity = Gravity.CENTER_VERTICAL
        sumLayoutParams.rightMargin = 20
        sumLayoutParams.weight = 2.0F

        val categoryLayoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
        categoryLayoutParams.gravity = Gravity.CENTER
        categoryLayoutParams.weight = 2.0F

        val tableRow = TableRow(this)
        tableRow.layoutParams = tableParams// TableLayout is the parent view[

        val dateView = TextView(this)
        dateView.gravity = Gravity.CENTER
        dateView.layoutParams = dateLayoutParams// TableRow is the parent view
        dateView.text = date
        tableRow.addView(dateView)

        val sumView = TextView(this)
        sumView.gravity = Gravity.LEFT
        sumView.layoutParams = sumLayoutParams// TableRow is the parent view
        sumView.text = sum
        tableRow.addView(sumView)

        val categoryView = TextView(this)
        categoryView.layoutParams = categoryLayoutParams// TableRow is the parent view
        categoryView.text = category
        tableRow.addView(categoryView)

        return tableRow
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        updateTable()
    }

    override fun onStart() {
        super.onStart()
        updateTable()
    }
}
