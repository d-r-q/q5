package ru.jdev.q5

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import ru.jdev.q5.Transaction.Companion.parse
import ru.jdev.q5.storage.QCollection
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

private val UTF_8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

class TransactionLog(private val context: Context) {

    private val trxFileNameFormat = SimpleDateFormat("yyMM'-${Build.DEVICE}-v2.csv'")

    fun storeTrx(logPart: String?, trx: Transaction): Boolean {
        if (!isExternalStorageWritable()) {
            return false
        }

        val file = File(context.getExternalFilesDir(null), logPart ?: trxFileNameFormat.format(Date()))
        Log.d("storeTrx", "${file.parentFile.exists()}")
        if (!file.parentFile.exists()) {
            Log.d("storeTrx", "Creating Q5 dir")
            file.parentFile.mkdirs()
        }
        LogPart(file).with(trx)
        return true
    }

    fun part(logPart: String): LogPart = LogPart(File(context.getExternalFilesDir(null), logPart))

    fun partNames(): List<String> {
        Log.d("transactionLog", "partsFiles")
        if (!(context.getExternalFilesDir(null)?.exists() ?: false)) {
            return listOf()
        }
        return context.getExternalFilesDir(null)
                .listFiles({ file -> file.name.endsWith(".csv") })
                .map { it.name }
    }

    fun parts() = partNames()
            .map { part(it) }

    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            return true
        }
        return false
    }

}

class LogPart(private val content: File) {

    val name: String = content.name
    private val transactions = QCollection<Transaction>(content, { line -> parse(line) }, { it -> it.toCsvLine() })

    fun list(): List<Transaction> = transactions.list()

    fun with(trx: Transaction): LogPart {
        transactions.with(trx)
        transactions.persist()
        return this
    }

    fun sharableView(): ByteArray {
        val file = content
        Log.d("LogPart", "Sharing file: ${file.name}")
        if (!file.exists()) {
            return UTF_8_BOM
        }
        val out = ByteArrayOutputStream()
        val writer = BufferedWriter(OutputStreamWriter(out, "UTF-8"))
        out.write(UTF_8_BOM)
        BufferedReader(InputStreamReader(FileInputStream(file), "UTF-8")).lineSequence()
                .forEach { writer.write(it); writer.newLine() }
        writer.flush()
        Log.d("LogPart", "Out: ${String(out.toByteArray())}")
        return out.toByteArray()
    }

    override fun toString(): String = name

}