package gui.forminputs

import config.Config
import gui.config.formcomponents.BooleanFormInput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BooleanFormInputTest {

    @Test
    fun testBooleanFormInputWithTrueValue() {
        val input = BooleanFormInput("enableSceneTimestampLogger", "label")
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
    fun testBooleanFormInputWithFalsevalue() {
        val input = BooleanFormInput("enableSceneTimestampLogger", "label")
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