package ru.jdev.q5

import android.content.Context
import android.util.Log
import ru.jdev.q5.storage.QCollection
import java.io.File

@Deprecated("Use QTransactionLog")
class TransactionLog(private val context: Context) {

    fun part(logPart: String): LogPart = LogPart(File(context.getExternalFilesDir(null), logPart))

    fun partNames(): List<String> {
        Log.d("transactionLog", "partsFiles")
        if (context.getExternalFilesDir(null)?.exists() != true) {
            return listOf()
        }
        return context.getExternalFilesDir(null)!!
                .listFiles { file -> file.name.endsWith(".csv") }
                .map { it.name }
    }

}

@Deprecated("Use QTransactionLog.monthTrxs")
class LogPart(content: File) {

    val name: String = content.name
    private val transactions = QCollection(content) { Transaction.parse(it) }

    fun list(): List<Transaction> = transactions.list()

    override fun toString(): String = name

}
