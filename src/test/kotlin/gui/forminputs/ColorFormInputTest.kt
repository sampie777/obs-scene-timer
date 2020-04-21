package gui.forminputs

import config.Config
import gui.config.formcomponents.ColorFormInput
import java.awt.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ColorFormInputTest {

    @Test
    fun testColorFormInput() {
        val input = ColorFormInput("timerBackgroundColor", "label")
        Config.timerBackgroundColor = Color.RED
        input.component()

        assertEquals(Color.RED, input.value())
        assertTrue(input.validate().isEmpty())

        Config.timerBackgroundColor = Color.BLACK
        input.save()
        assertEquals(Color.RED, input.value())
        assertEquals(Color.RED, Config.timerBackgroundColor)
    }
}