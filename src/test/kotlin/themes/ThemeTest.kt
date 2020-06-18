package themes

import config.Config
import mocks.ThemeMock
import java.awt.Color
import javax.swing.UIManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Suppress("DEPRECATION")
class ThemeTest {

    @Test
    fun testSetAndApplyTheme() {
        Config.theme = "LightTheme"
        Theme.init()

        // Then
        assertEquals(LightTheme().BACKGROUND_COLOR, UIManager.get("Panel.background"))

        // When
        Config.theme = "DarkTheme"
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
        assertEquals(LightTheme().BACKGROUND_COLOR, UIManager.get("Panel.background"))
    }

    @Test
    fun testAddNewThemeAndSetAndApplyTheme() {
        Config.theme = "LightTheme"
        Theme.init()

        // Then
        assertEquals(2, Theme.availableThemes().size)
        assertEquals(LightTheme().BACKGROUND_COLOR, UIManager.get("Panel.background"))

        // When
        Theme.addTheme("ThemeMock", "Mock Theme", ThemeMock::class.java)
        Config.theme = "ThemeMock"
        Theme.init()

        // Then
        assertEquals(3, Theme.availableThemes().size)
        assertEquals(Color.RED, UIManager.get("Panel.background"))
    }

    @Test
    fun testGetDeprecatedTimerConfigColorIfCustomSpecified() {
        Config.timerBackgroundColor = Color.BLUE
        Config.approachingLimitColor = Color.GREEN
        Config.exceededLimitColor = Color.PINK

        assertEquals(Color.BLUE, Theme.getTimerDefaultBackgroundColor())
        assertEquals(Color.GREEN, Theme.getTimerApproachingBackgroundColor())
        assertEquals(Color.PINK, Theme.getTimerExceededBackgroundColor())

        assertNotNull(Config.timerBackgroundColor)
        assertNotNull(Config.approachingLimitColor)
        assertNotNull(Config.exceededLimitColor)
    }

    @Test
    fun testGetThemeTimerColorIfDefaultSpecifiedInConfig() {
        Config.timerBackgroundColor = Color(230,230,230)
        Config.approachingLimitColor = Color.ORANGE
        Config.exceededLimitColor = Color.RED

        assertEquals(Theme.get.BACKGROUND_COLOR, Theme.getTimerDefaultBackgroundColor())
        assertEquals(Theme.get.TIMER_APPROACHING_BACKGROUND_COLOR, Theme.getTimerApproachingBackgroundColor())
        assertEquals(Theme.get.TIMER_EXCEEDED_BACKGROUND_COLOR, Theme.getTimerExceededBackgroundColor())

        assertNull(Config.timerBackgroundColor)
        assertNull(Config.approachingLimitColor)
        assertNull(Config.exceededLimitColor)
    }

    @Test
    fun testGetThemeTimerColorIfNotSpecifiedInConfig() {
        Config.timerBackgroundColor = null
        Config.approachingLimitColor = null
        Config.exceededLimitColor = null

        assertEquals(Theme.get.BACKGROUND_COLOR, Theme.getTimerDefaultBackgroundColor())
        assertEquals(Theme.get.TIMER_APPROACHING_BACKGROUND_COLOR, Theme.getTimerApproachingBackgroundColor())
        assertEquals(Theme.get.TIMER_EXCEEDED_BACKGROUND_COLOR, Theme.getTimerExceededBackgroundColor())

        assertNull(Config.timerBackgroundColor)
        assertNull(Config.approachingLimitColor)
        assertNull(Config.exceededLimitColor)
    }
}