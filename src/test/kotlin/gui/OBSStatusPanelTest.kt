package gui

import config.Config
import obs.OBSClientStatus
import obs.OBSConnectionStatus
import obs.OBSState
import kotlin.test.*

class OBSStatusPanelTest {

    @BeforeTest
    fun before() {
        Config.obsAddress = "ws://localhost:4444"
        OBSState.clientActivityStatus = null
        OBSState.connectionStatus = OBSConnectionStatus.UNKNOWN
    }

    @Test
    fun testGetOBSStatusRepresentationWithUnkownStatus() {
        val panel = OBSStatusPanel()

        OBSState.connectionStatus = OBSConnectionStatus.UNKNOWN

        assertEquals("Unknown", panel.getOBSStatusRepresentation())
    }

    @Test
    fun testGetOBSStatusRepresentationWithConnectingStatus() {
        val panel = OBSStatusPanel()

        OBSState.connectionStatus = OBSConnectionStatus.CONNECTING

        assertEquals("Connecting to ${Config.obsAddress}...", panel.getOBSStatusRepresentation())
    }

    @Test
    fun testGetOBSStatusRepresentationWithConnectedStatus() {
        val panel = OBSStatusPanel()

        OBSState.connectionStatus = OBSConnectionStatus.CONNECTED

        assertEquals("Connected", panel.getOBSStatusRepresentation())
    }

    @Test
    fun testGetOBSStatusRepresentationWithDisconnectedStatus() {
        val panel = OBSStatusPanel()

        OBSState.connectionStatus = OBSConnectionStatus.DISCONNECTED

        assertEquals("Disconnected", panel.getOBSStatusRepresentation())
    }

    @Test
    fun testGetOBSStatusRepresentationWithConnectionFailedStatus() {
        val panel = OBSStatusPanel()

        OBSState.connectionStatus = OBSConnectionStatus.CONNECTION_FAILED

        assertEquals("Connection failed!", panel.getOBSStatusRepresentation())
    }

    @Test
    fun testGetOBSStatusRepresentationWithLoadingScenesStatus() {
        val panel = OBSStatusPanel()

        OBSState.clientActivityStatus = OBSClientStatus.LOADING_SCENES
        OBSState.connectionStatus = OBSConnectionStatus.CONNECTED

        assertEquals("Loading scenes...", panel.getOBSStatusRepresentation())
    }

    @Test
    fun testMessageLabelWithRefreshingOBSStatus() {
        OBSState.connectionStatus = OBSConnectionStatus.UNKNOWN
        val panel = OBSStatusPanel()

        assertEquals("OBS: Unknown", panel.getMessageLabel().text)
        assertFalse(panel.getMessageLabel().toolTipText.contains("Connected"),
            "'Connected' string is falsy in messageLabel tooltip text")

        OBSState.connectionStatus = OBSConnectionStatus.CONNECTED
        panel.refreshOBSStatus()

        assertEquals("OBS: Connected", panel.getMessageLabel().text)
        assertTrue(panel.getMessageLabel().toolTipText.contains("Connected"),
            "'Connected' string is missing in messageLabel tooltip text")
    }
}