package nl.sajansen.obsscenetimer.gui.mainFrame

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.waitForSwing
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MainFramePanelTest {

    @Test
    fun testNotificationButtonChangesOnNewNotifications() {
        val panel = MainFramePanel()

        waitForSwing()
        Notifications.markAllAsRead()

        waitForSwing()
        assertEquals("", panel.notificationsButton.text)
        assertEquals("No new notifications", panel.notificationsButton.toolTipText)

        Notifications.add("message")

        waitForSwing()
        assertEquals("(1)", panel.notificationsButton.text)
        assertEquals("New notifications available", panel.notificationsButton.toolTipText)

        Notifications.markAllAsRead()

        waitForSwing()
        assertEquals("", panel.notificationsButton.text)
        assertEquals("No new notifications", panel.notificationsButton.toolTipText)
    }

    @Test
    fun testSavingDividerPositionOnWindowClosing() {
        Config.mainPanelDividerLocation = 0
        val panel = MainFramePanel()
        panel.dividerLocation = 100

        panel.windowClosing(null)

        assertEquals(100, Config.mainPanelDividerLocation)
    }

    @Test
    fun testRestoringDividerPosition() {
        Config.mainPanelDividerLocation = 1
        Config.windowRestoreLastPosition = true

        val panel = MainFramePanel()

        assertEquals(1, panel.dividerLocation)
    }

    @Test
    fun testNotRestoringDividerPosition() {
        Config.mainPanelDividerLocation = 1
        Config.windowRestoreLastPosition = false

        val panel = MainFramePanel()

        assertNotEquals(1, panel.dividerLocation)
    }
}