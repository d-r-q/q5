package ru.jdev.q5.categories

import android.database.sqlite.SQLiteDatabase

class Categories(private val db: SQLiteDatabase) {

    fun listAll(): List<Category> {
        val cursor = db.query("categories", arrayOf("id", "name"), null, null, null, null, null)
        try {
            val res = ArrayList<Category>()
            cursor.moveToFirst()
            do {
                res.add(Category(cursor.getLong(0), cursor.getString(1)))
            } while (cursor.moveToNext())
            return res
        } finally {
            cursor.close()
        }
    }
}

data class Category(val id: Long, val name: String)