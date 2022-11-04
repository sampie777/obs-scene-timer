package nl.sajansen.obsscenetimer.gui.websocketScanner

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.objects.websocketScanner.ScanResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WebsocketScannerFrameTest {

    @Test
    fun testSaveReturnsTrueOnValidData() {
        Config.obsAddress = "some address"
        Config.obsHost = "some address"
        Config.obsPort = 0
        val frame = WebsocketScannerFrame(null, visible = false)
        frame.websocketScannerTable.addScanResult(ScanResult("address1", 123, true))
        frame.websocketScannerTable.table.setRowSelectionInterval(0, 0)

        assertTrue(frame.save())
        assertEquals("ws://address1:123", Config.obsAddress)
        assertEquals("address1", Config.obsHost)
        assertEquals(123, Config.obsPort)
    }

    @Test
    fun testSaveReturnsFalseOnNulLData() {
        Config.obsAddress = "some address"
        Config.obsHost = "some address"
        Config.obsPort = 0
        val frame = WebsocketScannerFrame(null, visible = false)
        frame.websocketScannerTable.clearTable()

        assertFalse(frame.save())
        assertEquals("some address", Config.obsAddress)
        assertEquals("some address", Config.obsHost)
        assertEquals(0, Config.obsPort)
    }
}