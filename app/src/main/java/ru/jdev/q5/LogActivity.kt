package ru.jdev.q5

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.content.FileProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import org.jetbrains.anko.find
import org.jetbrains.anko.onClick
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class LogActivity : AppCompatActivity() {

    private val dateFormat = SimpleDateFormat("dd/MMM")
    private val dd_mm_yy = SimpleDateFormat("dd.MM.yy")
    private val log = TransactionLog(this)
    private val tableParams = FrameLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)
    private var data: List<Transaction> = listOf()

    private lateinit var fromDate: Date
    private lateinit var toDate: Date

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

        find<FloatingActionButton>(R.id.fab).onClick {
            val configIntent = Intent(this, EnterSumActivity::class.java)
            configIntent.putExtra(EnterSumActivity.sourceExtra, "manual")
            startActivity(configIntent)
        }
        val now = Calendar.getInstance()

        now.set(Calendar.DAY_OF_MONTH, 1)
        fromDate = savedInstanceState?.getSerializable("fromDate") as? Date ?: now.time
        now.set(Calendar.DAY_OF_MONTH, now.getActualMaximum(Calendar.DAY_OF_MONTH))
        toDate = savedInstanceState?.getSerializable("toDate") as? Date ?: now.time

        val fromDatePicker = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            now.set(Calendar.YEAR, year)
            now.set(Calendar.MONTH, monthOfYear)
            now.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            now.set(Calendar.HOUR_OF_DAY, 0)
            now.set(Calendar.MINUTE, 0)
            now.set(Calendar.SECOND, 0)
            now.set(Calendar.MILLISECOND, 0)
            fromDate = now.time
            with(find<EditText>(R.id.from_date)) {
                val text = String.format("%02d.%02d.%d", dayOfMonth, monthOfYear + 1, year)
                setText(text)
            }
            updateTable()
        }

        val toDatePicker = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            now.set(Calendar.YEAR, year)
            now.set(Calendar.MONTH, monthOfYear)
            now.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            now.set(Calendar.HOUR_OF_DAY, 23)
            now.set(Calendar.MINUTE, 59)
            now.set(Calendar.SECOND, 59)
            now.set(Calendar.MILLISECOND, 999)
            toDate = now.time
            with(find<EditText>(R.id.to_date)) {
                val text = String.format("%02d.%02d.%d", dayOfMonth, monthOfYear + 1, year)
                setText(text)
            }
            updateTable()
        }

        with(find<EditText>(R.id.from_date)) {
            setText(dd_mm_yy.format(fromDate))
            isFocusable = false
            setOnClickListener {
                val myCalendar = Calendar.getInstance()
                myCalendar.time = fromDate
                DatePickerDialog(this@LogActivity, fromDatePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show()
            }
        }
        with(find<EditText>(R.id.to_date)) {
            setText(dd_mm_yy.format(toDate))
            isFocusable = false
            setOnClickListener {
                val myCalendar = Calendar.getInstance()
                myCalendar.time = toDate
                DatePickerDialog(this@LogActivity, toDatePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show()
            }
        }
        updateTable()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_log, menu)
        return true;
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
        val outputFile = File(applicationContext.externalCacheDir, find<EditText>(R.id.from_date).text.toString() + "-" + find<EditText>(R.id.to_date).text.toString() + ".csv")
        val utf8bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        val fos = FileOutputStream(outputFile)
        BufferedWriter(OutputStreamWriter(fos)).use { out ->
            fos.write(utf8bom)
            data.forEach {
                out.write(it.toExternalCsvLine())
                out.newLine()
            }
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

    private val trxDateComparator = Comparator<Transaction> { (_, date1), (_, date2) -> date1.dateTime.compareTo(date2.dateTime) }
    private val trxSumComparator = Comparator<Transaction> { (_, _, sum1), (_, _, sum2) -> BigDecimal(sum1.replace(',', '.')).compareTo(BigDecimal(sum2.replace(',', '.'))) }
    private var trxComparator = trxDateComparator

    private fun updateTable() {
        val table = find<TableLayout>(R.id.table)

        table.removeAllViews()
        // сейчас что сортировка по дате, что по сумме интересует по убыванию, так что компараторы сортируют по возрастанию,
        // а "оборачиваем" здесь
        data = log.find(fromDate, toDate)
        val trxes = ArrayList(data).sortedWith(trxComparator).asReversed()
        trxes.forEach { trx ->
            val row = createRow(dateFormat.format(trx.date.dateTime), trx.sum, trx.category)
            row.onClick {
                val configIntent = Intent(this, EnterSumActivity::class.java)
                configIntent.putExtra(EnterSumActivity.logPartExtra, trx.logPart)
                configIntent.putExtra(EnterSumActivity.trxIdExtra, trx.id)
                configIntent.putExtra("sum", trx.sum)
                configIntent.putExtra("category", trx.category)
                configIntent.putExtra("comment", trx.comment)
                configIntent.putExtra("date", trx.date.date())
                configIntent.putExtra("time", trx.date.time())
                startActivity(configIntent)
            }
            table.addView(row)
        }

        val fmt = NumberFormat.getCurrencyInstance()
        val row = createRow("", fmt.format(trxes.sumByDouble { it.sum.replace(',', '.').toDouble() }), "Итого")
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
