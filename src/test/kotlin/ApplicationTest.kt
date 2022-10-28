import config.Config
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
}