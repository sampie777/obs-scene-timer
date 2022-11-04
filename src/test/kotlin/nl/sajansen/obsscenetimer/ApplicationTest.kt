package nl.sajansen.obsscenetimer

import nl.sajansen.obsscenetimer.config.Config
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun `test config obs host is set when obsAddress differs`() {
        Config.obsAddress = "ws://test:1234"
        Config.obsHost = ""
        Config.obsPort = 0

        setObsParametersFromObsAddress()

        assertEquals("test", Config.obsHost)
        assertEquals(1234, Config.obsPort)
    }

    @Test
    fun `test config obs host remains when obsAddress is equal`() {
        Config.obsAddress = "ws://test:1234"
        Config.obsHost = "test"
        Config.obsPort = 1234

        setObsParametersFromObsAddress()

        assertEquals("test", Config.obsHost)
        assertEquals(1234, Config.obsPort)
    }

    @Test
    fun `test config obs port defaults when obsAddress has non integer port`() {
        Config.obsAddress = "ws://test:abc"
        Config.obsHost = ""
        Config.obsPort = 1234

        setObsParametersFromObsAddress()

        assertEquals("test", Config.obsHost)
        assertEquals(4455, Config.obsPort)
    }

    @Test
    fun `test config obs port defaults when obsAddress has no port`() {
        Config.obsAddress = "ws://test"
        Config.obsHost = ""
        Config.obsPort = 1234

        setObsParametersFromObsAddress()

        assertEquals("test", Config.obsHost)
        assertEquals(4455, Config.obsPort)
    }

    @Test
    fun `test config obs host remains when obsAddress is empty`() {
        Config.obsAddress = ""
        Config.obsHost = "test"
        Config.obsPort = 1234

        setObsParametersFromObsAddress()

        assertEquals("test", Config.obsHost)
        assertEquals(1234, Config.obsPort)
    }
    
    @Test
    fun `test config remoteSync host is set when remoteSyncClientAddress differs`() {
        Config.remoteSyncClientAddress = "ws://test:1234"
        Config.remoteSyncServerHost = ""
        Config.remoteSyncServerPort = 0

        setObsParametersFromObsAddress()

        assertEquals("test", Config.remoteSyncServerHost)
        assertEquals(1234, Config.remoteSyncServerPort)
    }

    @Test
    fun `test config remoteSync host remains when remoteSyncClientAddress is equal`() {
        Config.remoteSyncClientAddress = "ws://test:1234"
        Config.remoteSyncServerHost = "test"
        Config.remoteSyncServerPort = 1234

        setObsParametersFromObsAddress()

        assertEquals("test", Config.remoteSyncServerHost)
        assertEquals(1234, Config.remoteSyncServerPort)
    }

    @Test
    fun `test config remoteSync port defaults when remoteSyncClientAddress has non integer port`() {
        Config.remoteSyncClientAddress = "ws://test:abc"
        Config.remoteSyncServerHost = ""
        Config.remoteSyncServerPort = 1234

        setObsParametersFromObsAddress()

        assertEquals("test", Config.remoteSyncServerHost)
        assertEquals(4050, Config.remoteSyncServerPort)
    }

    @Test
    fun `test config remoteSync port defaults when remoteSyncClientAddress has no port`() {
        Config.remoteSyncClientAddress = "ws://test"
        Config.remoteSyncServerHost = ""
        Config.remoteSyncServerPort = 1234

        setObsParametersFromObsAddress()

        assertEquals("test", Config.remoteSyncServerHost)
        assertEquals(4050, Config.remoteSyncServerPort)
    }

    @Test
    fun `test config remoteSync host remains when remoteSyncClientAddress is empty`() {
        Config.remoteSyncClientAddress = ""
        Config.remoteSyncServerHost = "test"
        Config.remoteSyncServerPort = 1234

        setObsParametersFromObsAddress()

        assertEquals("test", Config.remoteSyncServerHost)
        assertEquals(1234, Config.remoteSyncServerPort)
    }
}