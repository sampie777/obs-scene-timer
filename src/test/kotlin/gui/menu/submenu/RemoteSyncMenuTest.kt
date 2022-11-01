package gui.menu.submenu

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.gui.menu.submenu.RemoteSyncMenu
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.remotesync.server.ServerStatus
import org.eclipse.jetty.websocket.api.*
import resetConfig
import java.net.InetSocketAddress
import kotlin.test.*


class RemoteSyncMenuTest {

    @BeforeTest
    fun before() {
        resetConfig()
        Notifications.clear()
    }

    @Test
    fun testUpdateMenuItemsSetsEnabledCorrectly() {
        val menu = RemoteSyncMenu()

        assertTrue(menu.startServerItem.isEnabled)
        assertFalse(menu.stopServerItem.isEnabled)
        assertTrue(menu.startClientItem.isEnabled)
        assertFalse(menu.stopClientItem.isEnabled)

        Config.remoteSyncServerEnabled = false
        Config.remoteSyncClientEnabled = true
        menu.updateMenuItems()

        assertTrue(menu.startServerItem.isEnabled)
        assertFalse(menu.stopServerItem.isEnabled)
        assertFalse(menu.startClientItem.isEnabled)
        assertTrue(menu.stopClientItem.isEnabled)

        Config.remoteSyncServerEnabled = true
        Config.remoteSyncClientEnabled = false
        menu.updateMenuItems()

        assertFalse(menu.startServerItem.isEnabled)
        assertTrue(menu.stopServerItem.isEnabled)
        assertTrue(menu.startClientItem.isEnabled)
        assertFalse(menu.stopClientItem.isEnabled)
    }

    @Test
    fun testUpdateSessionCount() {
        val menu = RemoteSyncMenu()
        assertEquals("", menu.stopServerItem.toolTipText)

        Config.remoteSyncServerEnabled = true
        menu.updateMenuItems()
        assertEquals("0 connections", menu.stopServerItem.toolTipText)

        ServerStatus.clients["x"] = object : Session{
            override fun getRemote(): RemoteEndpoint {
                TODO("Not yet implemented")
            }

            override fun getLocalAddress(): InetSocketAddress {
                TODO("Not yet implemented")
            }

            override fun disconnect() {
                TODO("Not yet implemented")
            }

            override fun getProtocolVersion(): String {
                TODO("Not yet implemented")
            }

            override fun getUpgradeResponse(): UpgradeResponse {
                TODO("Not yet implemented")
            }

            override fun setIdleTimeout(p0: Long) {
                TODO("Not yet implemented")
            }

            override fun getPolicy(): WebSocketPolicy {
                TODO("Not yet implemented")
            }

            override fun getUpgradeRequest(): UpgradeRequest {
                TODO("Not yet implemented")
            }

            override fun suspend(): SuspendToken {
                TODO("Not yet implemented")
            }

            override fun isOpen(): Boolean {
                TODO("Not yet implemented")
            }

            override fun getIdleTimeout(): Long {
                TODO("Not yet implemented")
            }

            override fun close() {
                TODO("Not yet implemented")
            }

            override fun close(p0: CloseStatus?) {
                TODO("Not yet implemented")
            }

            override fun close(p0: Int, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun isSecure(): Boolean {
                TODO("Not yet implemented")
            }

            override fun getRemoteAddress(): InetSocketAddress {
                TODO("Not yet implemented")
            }
        }
        menu.remoteSyncServerConnectionsUpdate()
        assertEquals("1 connections", menu.stopServerItem.toolTipText)

        Config.remoteSyncServerEnabled = false
        menu.updateMenuItems()
        assertEquals("", menu.stopServerItem.toolTipText)
    }

    @Test
    fun testStopTimer() {
        Config.remoteSyncClientEnabled = true
        val menu = RemoteSyncMenu()
        menu.stopClientItem.doClick()

        assertFalse(Config.remoteSyncClientEnabled)
        assertTrue(menu.startClientItem.isEnabled)
        assertFalse(menu.stopClientItem.isEnabled)

        assertEquals(1, Notifications.unreadNotifications)
        assertEquals("Please restart the application to (re)connect to OBS", Notifications.list[0].message)
    }

    @Test
    fun testStopServer() {
        Config.remoteSyncServerEnabled = true
        val menu = RemoteSyncMenu()
        menu.stopServerItem.doClick()

        assertFalse(Config.remoteSyncServerEnabled)
        assertTrue(menu.startServerItem.isEnabled)
        assertFalse(menu.stopServerItem.isEnabled)
        assertEquals("", menu.stopServerItem.toolTipText)

        assertEquals(1, Notifications.unreadNotifications)
        assertEquals("Remote sync server stopped", Notifications.list[0].message)
    }
}