package ru.jdev.q5

import org.junit.Assert
import org.junit.Test
import ru.jdev.q5.gathering.parseSms

class IncomingSmsTest {

    @Test
    fun testParseAlfaBankRUR180915() {
        var res = parseSms("**0000 Pokupka Uspeshno Summa: 0 000 RUR Ostatok: 0 000,00 RUR RU/KOLTSOVO/TONUS 15.09.2018 08:47:30")
        Assert.assertNotNull(res)
        Assert.assertEquals("0000", res!!.sum)
        Assert.assertEquals("RU/KOLTSOVO/TONUS", res.place)

        res = parseSms("**0000 Pokupka Uspeshno Summa: 000,00 RUR Ostatok: 0 000,00 RUR RU/NOVOSIBIRSKAY/\"MYASNOY FORMAT\" SHOP 15.09.2018 08:51:34")
        Assert.assertNotNull(res)
        Assert.assertEquals("000,00", res!!.sum)
        Assert.assertEquals("RU/NOVOSIBIRSKAY/\"MYASNOY FORMAT\" SHOP", res.place)
    }

}
