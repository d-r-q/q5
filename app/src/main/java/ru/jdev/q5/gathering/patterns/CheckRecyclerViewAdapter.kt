package ru.jdev.q5.gathering.patterns

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener

import kotlinx.android.synthetic.main.check_item.view.*
import ru.jdev.q5.EditCheckParserForm
import ru.jdev.q5.R

class CheckRecyclerViewAdapter(private val form: EditCheckParserForm)
    : RecyclerView.Adapter<CheckRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.check_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.checkView.setText(form.checkExamples[position])

        showParseResults(holder, position)
    }

    private fun showParseResults(holder: ViewHolder, position: Int) {
        holder.matchesView.text = "Нет совпадения"
        holder.sumView.text = ""
        holder.placeView.text = ""
        holder.matchesView.setTextColor(Color.RED)
        val item = form.parseResults()[position] ?: return

        holder.matchesView.setTextColor(holder.sumView.currentTextColor)
        holder.matchesView.text = "Есть совпадение"
        holder.sumView.text = "Сумма: ${item.sum}"
        holder.placeView.text = "Место: ${item.place ?: "Не определено"}"
    }

    override fun getItemCount(): Int = form.checkExamples.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val checkView: EditText = view.checkInputName
        val matchesView: TextView = view.matchesLabelName
        val sumView: TextView = view.sumLabelName
        val placeView: TextView = view.placeLabelName

        init {
            checkView.addTextChangedListener {
                form.onCheckExampleUpdated(it.toString(), adapterPosition)
                showParseResults(this, adapterPosition)
            }
        }

    }
}