package ru.jdev.q5

import org.junit.Assert
import org.junit.Test
import java.util.*

class TransactionsParsingTest {

    private val months = listOf(2, 3, 4, 5, 6, 7, 8)
    private val devices = listOf("F5321", "OnePlus3")

    @Test
    fun testTransactionsParsing() {
        devices.forEach { device ->
            months.forEach { month ->
                val lines = javaClass.getResourceAsStream("/transaction_parsing/170$month-$device-v2.csv")?.bufferedReader()?.lineSequence()
                lines
                        ?.filter { it.trim().isNotEmpty() }
                        ?.forEachIndexed { idx, str ->
                            val t = QTransaction.parse(IndexedValue(idx, str))
                            if (str.contains("\",\"")) {
                                val c = Calendar.getInstance()
                                c.time = t.date.dateTime
                                Assert.assertEquals(str, 2017, c.get(Calendar.YEAR))
                                Assert.assertEquals(str, month - 1, c.get(Calendar.MONTH))
                                Assert.assertNotEquals(QTransaction.notParsed, t.sum)
                                Assert.assertNotEquals(QTransaction.notParsed, t.category)
                                Assert.assertNotEquals(QTransaction.notParsed, t.comment)
                                Assert.assertNotEquals(QTransaction.notParsed, t.device)
                                Assert.assertNotEquals(QTransaction.notParsed, t.source)
                            } else {
                                Assert.assertEquals(str.replace("\uFEFF", ""), t.toCsvLine())
                            }
                        }
            }
        }
    }
}