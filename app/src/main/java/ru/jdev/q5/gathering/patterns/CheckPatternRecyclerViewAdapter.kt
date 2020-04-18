package ru.jdev.q5.gathering.patterns

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.check_pattern_item.view.*
import org.jetbrains.anko.onClick
import ru.jdev.q5.R
import ru.jdev.q5.gathering.CheckPatterns


class CheckPatternRecyclerViewAdapter(private val checkPatterns: CheckPatterns)
    : RecyclerView.Adapter<CheckPatternRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.check_pattern_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int =
            checkPatterns.listPatterns().size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.patternName.text = ""
        holder.pattern.text = ""
        val pattern = checkPatterns.listPatterns().elementAtOrNull(position) ?: return

        holder.patternName.text = pattern.name
        holder.pattern.text = pattern.pattern.toString()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val patternName: TextView = view.patternNameText
        val pattern: TextView = view.patternText
        val delete: Button = view.deleteButton

        init {
            patternName.onClick {

                val configIntent = Intent(view.context, EditCheckPatternActivity::class.java)
                configIntent.putExtra("id", checkPatterns.listPatterns()[adapterPosition].id)
                view.context.startActivity(configIntent)
            }

            delete.onClick {

                val p = checkPatterns.listPatterns()[adapterPosition]
                val builder = AlertDialog.Builder(view.context)
                builder.setTitle("Подтвердите удаление")
                builder.setMessage("Удалить шаблон:\n ${p.name}")
                builder.setPositiveButton("Да") { _, _ ->
                    checkPatterns.delete(p.id)
                    this@CheckPatternRecyclerViewAdapter.notifyDataSetChanged()
                }
                builder.setNegativeButton("Нет") { _, _ -> /* noop */ }
                val dialog = builder.create()
                dialog.show()
            }
        }

    }

}