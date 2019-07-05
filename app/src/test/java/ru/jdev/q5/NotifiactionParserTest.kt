package ru.jdev.q5

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import ru.jdev.q5.gathering.parseNotification


class NotifiactionParserTest {

    @Test
    internal fun testGPay() {
        val check = parseNotification("Travelers coffee", "682,00 \u20BD с карты Mastercard **** 0000")
        assertNotNull(check)
        assertEquals("Travelers coffee", check!!.place)
        assertEquals("682,00", check.sum)
    }

    @Test
    internal fun testGPay2() {
        val check = parseNotification("Travelers coffee", "1 682,00 \u20BD с карты Mastercard **** 0000")
        assertNotNull(check)
        assertEquals("Travelers coffee", check!!.place)
        assertEquals("1 682,00", check.sum)
    }

    @Test
    fun testKukuruza() {
        val check = parseNotification("Кукуруза",
                """-215.00 RUR
                  |CKASSA, YAROSLAVL. Остаток  1 638.19 RUR. Карта *0000"""
                        .trimMargin())
        assertNotNull(check)
        assertEquals("CKASSA, YAROSLAVL.", check!!.place)
        assertEquals("215.00", check.sum)
    }

    @Test
    fun testKukuruza2() {
        val check = parseNotification("Кукуруза",
                """-1 215.00 RUR
                  |CKASSA, YAROSLAVL. Остаток  1 638.19 RUR. Карта *0000"""
                        .trimMargin())
        assertNotNull(check)
        assertEquals("CKASSA, YAROSLAVL.", check!!.place)
        assertEquals("1 215.00", check.sum)
    }

    @Test
    fun testAlfa() {
        val check = parseNotification("Уведомление",
                "**0000 Pokupka Uspeshno Summa: 309,51 RUR Ostatok: 2 315,19 RUR RU/Novosibirsk/YARMARKA DOBRYANKA 04.07.2019 15:22:04")
        assertNotNull(check)
        assertEquals("RU/Novosibirsk/YARMARKA DOBRYANKA", check!!.place)
        assertEquals("309,51", check.sum)
    }

    @Test
    fun testAlfa2() {
        val check = parseNotification("Уведомление",
                "**0000 Pokupka Uspeshno Summa: 1 309,51 RUR Ostatok: 2 315,19 RUR RU/Novosibirsk/YARMARKA DOBRYANKA 04.07.2019 15:22:04")
        assertNotNull(check)
        assertEquals("RU/Novosibirsk/YARMARKA DOBRYANKA", check!!.place)
        assertEquals("1 309,51", check.sum)
    }

    @Test
    fun testSber1() {
        val check = parseNotification("Перевод Сбербанк Онлайн",
                "5 000 \u20BD - Баланс 00 000 \u20BD МИР ** 0677")
        assertNotNull(check)
        assertEquals("Сбербанк Онлайн", check!!.place)
        assertEquals("5 000", check.sum)
    }

    @Test
    fun testSber2() {
        val check = parseNotification("Покупка Градус",
                "139 \u20BD - Баланс 00 000 \u20BD МИР ** 0677")
        assertNotNull(check)
        assertEquals("Градус", check!!.place)
        assertEquals("139", check.sum)
    }
}