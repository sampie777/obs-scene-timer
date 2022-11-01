package gui.config

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.gui.config.ConfigEditPanel
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import org.junit.Before
import resetConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConfigEditPanelTest {

    @Before
    fun before() {
        resetConfig()
    }

    @Test
    fun testSaveAllValid() {
        Config.obsPassword = "somevalue"
        val panel = ConfigEditPanel()

        Config.obsPassword = ""
        assertTrue(panel.saveAll())
        assertEquals("somevalue", Config.obsPassword)
    }

    @Test
    fun testSaveAllInvalid() {
        Config.obsHost = ""
        val panel = ConfigEditPanel()

        Config.obsHost = "somevalue"
        assertFalse(panel.saveAll())
        assertEquals("somevalue", Config.obsHost)
    }

    @Test
    fun testComponentWithInvalidValuesWillGiveErrorNotification() {
        Notifications.clear()
        // Set invalid config value
        Config.set("timerCountUpFontSize", -9000)

        ConfigEditPanel()

        assertEquals(1, Notifications.unreadNotifications)
        assertTrue(
            Notifications.list[0].message.contains("timerCountUpFontSize"),
            "Notifications doesn't containt the proper message"
        )
    }
}