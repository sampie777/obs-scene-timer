package nl.sajansen.obsscenetimer.gui.config.forminputs

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.gui.config.formcomponents.BooleanFormInput
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BooleanFormInputTest {

    @AfterTest
    fun after() {
    }

    @Test
    fun testBooleanFormInputWithTrueValue() {
        val input = BooleanFormInput("enableSceneTimestampLogger", "label", onSave = null)
        Config.enableSceneTimestampLogger = true
        input.component()

        assertEquals(true, input.value())
        assertTrue(input.validate().isEmpty())

        Config.enableSceneTimestampLogger = false
        input.save()
        assertEquals(true, input.value())
        assertEquals(true, Config.enableSceneTimestampLogger)
    }

    @Test
    fun testBooleanFormInputWithFalseValue() {
        val input = BooleanFormInput("enableSceneTimestampLogger", "label", onSave = null)
        Config.enableSceneTimestampLogger = false
        input.component()

        assertEquals(false, input.value())
        assertTrue(input.validate().isEmpty())

        Config.enableSceneTimestampLogger = true
        input.save()
        assertEquals(false, input.value())
        assertEquals(false, Config.enableSceneTimestampLogger)
    }
}