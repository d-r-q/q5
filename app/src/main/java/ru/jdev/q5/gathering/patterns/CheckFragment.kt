package ru.jdev.q5.gathering.patterns

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.jdev.q5.EditCheckParserForm
import ru.jdev.q5.R

class CheckFragment(private val form: EditCheckParserForm) : Fragment() {

    lateinit var listView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        listView = inflater.inflate(R.layout.checks_list, container, false) as? RecyclerView
                ?: return null

        with(listView) {
            layoutManager = LinearLayoutManager(context)
            adapter = CheckRecyclerViewAdapter(form)
        }

        return listView
    }

}