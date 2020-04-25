package themes

import config.Config
import java.awt.Color
import javax.swing.UIManager
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeTest {

    @Test
    fun testSetAndApplyTheme() {
        Config.theme = "Default"
        Theme.init()

        // Then
        assertEquals(DefaultTheme().BACKGROUND_COLOR, UIManager.get("Panel.background"))

        // When
        Config.theme = "Dark"
        Theme.init()

        // Then
        assertEquals(DarkTheme().BACKGROUND_COLOR, UIManager.get("Panel.background"))
    }

    @Test
    fun testSetAndApplyNonExistingThemeDefaultsToDefaultTheme() {
        UIManager.put("Panel.background", Color(0, 100, 200))
        assertEquals(Color(0, 100, 200), UIManager.get("Panel.background"))

        Config.theme = "NonExistingTheme"
        Theme.init()

        // Then
        assertEquals(DefaultTheme().BACKGROUND_COLOR, UIManager.get("Panel.background"))
    }
}