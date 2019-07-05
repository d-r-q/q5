package ru.jdev.q5

import org.junit.Assert
import org.junit.Test
import ru.jdev.q5.gathering.parseSms

class IncomingSmsTest {

    @Test
    fun testParseAlfaBankRUR180915() {
        var res = parseSms("**3239 Pokupka Uspeshno Summa: 1 143 RUR Ostatok: 9 197,93 RUR RU/KOLTSOVO/TONUS 15.09.2018 08:47:30")
        Assert.assertNotNull(res)
        Assert.assertEquals("1143", res!!.sum)
        Assert.assertEquals("RU/KOLTSOVO/TONUS", res.place)

        res = parseSms("**3239 Pokupka Uspeshno Summa: 380,90 RUR Ostatok: 8 817,03 RUR RU/NOVOSIBIRSKAY/\"MYASNOY FORMAT\" SHOP 15.09.2018 08:51:34")
        Assert.assertNotNull(res)
        Assert.assertEquals("380,90", res!!.sum)
        Assert.assertEquals("RU/NOVOSIBIRSKAY/\"MYASNOY FORMAT\" SHOP", res.place)
    }

}
