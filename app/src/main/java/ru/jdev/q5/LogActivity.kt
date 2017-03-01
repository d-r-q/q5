package ru.jdev.q5

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import org.jetbrains.anko.find
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
    }

    private fun updateTable() {
        val table = find<TableLayout>(R.id.table)
        table.removeAllViews()
        log.list().forEach {
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

            val date = TextView(this)
            date.gravity = Gravity.CENTER
            date.layoutParams = dateLayoutParams// TableRow is the parent view
            date.text = dateFormat.format(it.date.dateTime)
            tableRow.addView(date)

            val sum = TextView(this)
            sum.gravity = Gravity.LEFT
            sum.layoutParams = sumLayoutParams// TableRow is the parent view
            sum.text = it.sum
            tableRow.addView(sum)

            val category = TextView(this)
            category.layoutParams = categoryLayoutParams// TableRow is the parent view
            category.text = it.category
            tableRow.addView(category)

            table.addView(tableRow)
        }
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        updateTable()
    }

    override fun onStart() {
        super.onStart()
        updateTable()
    }
}
