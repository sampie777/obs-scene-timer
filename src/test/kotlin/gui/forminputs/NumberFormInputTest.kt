package gui.forminputs

import config.Config
import gui.config.formcomponents.NumberFormInput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NumberFormInputTest {

    @Test
    fun testNumberFormInputWithInt() {
        val input = NumberFormInput<Int>("timerCountUpFontSize", "label", 0, 10)
        Config.timerCountUpFontSize = 5
        input.component()

        assertEquals(5, input.value())
        assertTrue(input.value() is Int)
        assertTrue(input.validate().isEmpty())

        Config.timerCountUpFontSize = 7
        input.save()
        assertEquals(5, Config.timerCountUpFontSize)
    }

    @Test
    fun testNumberFormInputWithLong() {
        val input = NumberFormInput<Long>("obsReconnectionTimeout", "label", 0, 10)
        Config.obsReconnectionTimeout = 5
        input.component()

        assertEquals(5, input.value())
        assertTrue(input.value() is Long)
        assertTrue(input.validate().isEmpty())

        Config.obsReconnectionTimeout = 7
        input.save()
        assertEquals(5, Config.obsReconnectionTimeout)
    }
}