package ru.jdev.q5

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import org.jetbrains.anko.find
import org.jetbrains.anko.onClick
import org.jetbrains.anko.onItemSelectedListener
import org.jetbrains.anko.toast
import java.text.NumberFormat
import java.text.SimpleDateFormat


class LogActivity : AppCompatActivity() {

    private val dateFormat = SimpleDateFormat("dd/MMM")
    val log = TransactionLog(this)
    val tableParams = FrameLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val table = find<TableLayout>(R.id.table)
        table.layoutParams = tableParams
        table.setColumnShrinkable(0, true)
        table.setColumnShrinkable(1, true)
        table.setColumnShrinkable(2, true)
        table.setColumnStretchable(2, true)
        updateTable()

        find<FloatingActionButton>(R.id.fab).onClick {
            val configIntent = Intent(this, EnterSumActivity::class.java)
            configIntent.putExtra(EnterSumActivity.sourceExtra, "manual")
            startActivity(configIntent)
        }
        with(find<Spinner>(R.id.log_part)) {
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, log.parts().sortedByDescending { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.adapter = adapter
            this.setSelection(0)
            this.onItemSelectedListener { onItemSelected { adapterView, view, i, l -> updateTable() } }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_log, menu);
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val part = find<Spinner>(R.id.log_part).selectedItem as LogPart?
        if (part == null) {
            toast("Не выбран журнал")
            return false
        }
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, String(part.sharableView()))
        sendIntent.type = "text/csv"
        startActivity(Intent.createChooser(sendIntent, "Отправить журнал"))

        return super.onOptionsItemSelected(item)
    }

    private fun updateTable() {
        val table = find<TableLayout>(R.id.table)

        table.removeAllViews()
        val part = find<Spinner>(R.id.log_part).selectedItem as LogPart?
        val trxes = (part?.list()?.toList() ?: listOf()).reversed()
        trxes.forEach {
            val row = createRow(dateFormat.format(it.date.dateTime), it.sum, it.category)
            table.addView(row)
        }

        val fmt = NumberFormat.getCurrencyInstance()
        val row = createRow("", fmt.format(trxes.sumByDouble { it.sum.replace(',', '.').toDouble() }), "Итого")
        table.addView(row)
    }

    private fun createRow(date: String, sum: String, category: String): TableRow {
        val dateLayoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
        dateLayoutParams.rightMargin = 40
        dateLayoutParams.weight = 1.0F

        val sumLayoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
        sumLayoutParams.rightMargin = 20
        sumLayoutParams.weight = 2.0F

        val categoryLayoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
        categoryLayoutParams.gravity = Gravity.CENTER
        categoryLayoutParams.weight = 2.0F

        val tableRow = TableRow(this)
        tableRow.layoutParams = tableParams// TableLayout is the parent view

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
