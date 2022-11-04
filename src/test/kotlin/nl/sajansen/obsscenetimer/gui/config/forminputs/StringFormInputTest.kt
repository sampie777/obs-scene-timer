package nl.sajansen.obsscenetimer.gui.config.forminputs

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.gui.config.formcomponents.StringFormInput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringFormInputTest {

    @Test
    fun testStringFormInput() {
        val input = StringFormInput("obsHost", "label", allowEmpty = false)
        Config.obsHost = "someaddress"
        input.component()

        assertEquals("someaddress", input.value())
        assertTrue(input.validate().isEmpty())

        Config.obsHost = ""
        input.save()
        assertEquals("someaddress", input.value())
        assertEquals("someaddress", Config.obsHost)
    }

    @Test
    fun testStringFormInputValidationWithWrongInput() {
        val input = StringFormInput("obsHost", "label", allowEmpty = false)
        Config.obsHost = ""
        input.component()

        assertEquals("", input.value())
        assertEquals(1, input.validate().size)

        Config.obsHost = "someaddress"

        assertEquals("", input.value())
        assertFalse(input.validate().isEmpty())
    }

    @Test
    fun testStringFormInputAllowEmptyValidationWithWrongInput() {
        val input = StringFormInput("obsHost", "label", allowEmpty = true)
        Config.obsHost = ""
        input.component()

        assertEquals("", input.value())
        assertTrue(input.validate().isEmpty())

        Config.obsHost = "someaddress"
        input.save()

        assertEquals("", input.value())
        assertEquals("", Config.obsHost)
    }
}