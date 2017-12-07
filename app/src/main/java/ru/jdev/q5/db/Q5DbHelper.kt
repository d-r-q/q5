package ru.jdev.q5.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ru.jdev.q5.CsvTransactionLog

private val dbVersion = 1

private val dbName = "q5.db"

class Q5DbHelper(private val ctx: Context) : SQLiteOpenHelper(ctx, dbName, null, dbVersion) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE categories (
                id integer primary key autoincrement,
                name text
            );
        """)

        db.execSQL("""
            CREATE TABLE transactions (
                id integer primary key autoincrement,
                timestamp int,
                sum decimal(7,2),
                category int,
                comment text,
                device text,
                source text,
                FOREIGN KEY (category) REFERENCES categories(id)
            );
        """)

        db.beginTransaction()
        try {
            val insertCategory = db.compileStatement("""
                INSERT INTO categories (name) VALUES (?)
                """)
            val insertTransaction = db.compileStatement("""
                INSERT INTO transactions (timestamp, sum, category, comment, device, source)
                VALUES (?, ?, ?, ?, ?, ?)
                """)
            val log = CsvTransactionLog(ctx)

            val categories = HashMap<String, Long>()
            log.parts().asSequence().flatMap { it.list() }.forEach { trx ->
                if (!categories.contains(trx.category)) {
                    insertCategory.bindString(1, trx.category)
                    val categoryId = insertCategory.executeInsert()
                    categories[trx.category] = categoryId
                }

                insertTransaction.bindLong(1, trx.date.dateTime.time)
                insertTransaction.bindString(2, trx.sum)
                insertTransaction.bindLong(3, categories[trx.category]!!)
                insertTransaction.bindString(4, trx.comment)
                insertTransaction.bindString(5, trx.device)
                insertTransaction.bindString(6, trx.source)

                insertTransaction.executeUpdateDelete()
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun deleteDb() {
        ctx.deleteDatabase(dbName)
    }

}