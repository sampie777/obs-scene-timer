package gui.config.forminputs

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.gui.config.formcomponents.BooleanFormInput
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BooleanFormInputTest {

    @AfterTest
    fun after() {
        Config.enableApplicationLoggingToFile = false
    }

    @Test
    fun testBooleanFormInputWithTrueValue() {
        val input = BooleanFormInput("enableApplicationLoggingToFile", "label", onSave = null)
        Config.enableApplicationLoggingToFile = true
        input.component()

        assertEquals(true, input.value())
        assertTrue(input.validate().isEmpty())

        Config.enableApplicationLoggingToFile = false
        input.save()
        assertEquals(true, input.value())
        assertEquals(true, Config.enableApplicationLoggingToFile)
    }

    @Test
    fun testBooleanFormInputWithFalseValue() {
        val input = BooleanFormInput("enableApplicationLoggingToFile", "label", onSave = null)
        Config.enableApplicationLoggingToFile = false
        input.component()

        assertEquals(false, input.value())
        assertTrue(input.validate().isEmpty())

        Config.enableApplicationLoggingToFile = true
        input.save()
        assertEquals(false, input.value())
        assertEquals(false, Config.enableApplicationLoggingToFile)
    }
}