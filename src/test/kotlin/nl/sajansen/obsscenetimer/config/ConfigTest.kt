package nl.sajansen.obsscenetimer.config

import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.resetConfig
import kotlin.test.*

class ConfigTest {

    @BeforeTest
    fun before() {
        resetConfig()
        Notifications.clear()
    }

    @Test
    fun testConfigGetExistingKey() {
        Config.obsHost = "x"

        assertEquals("x", Config.get("obsHost"))
        assertTrue(Notifications.list.isEmpty())
    }

    @Test
    fun testConfigGetNonExistingKey() {
        assertNull(Config.get("nonexistingkey"))
        assertEquals(1, Notifications.list.size)
        assertEquals("Could not get configuration setting: nonexistingkey (nonexistingkey)", Notifications.list[0].message)
    }

    @Test
    fun testConfigSetExistingKey() {
        Config.obsHost = "x"

        Config.set("obsHost", "new")
        assertEquals("new", Config.obsHost)
        assertTrue(Notifications.list.isEmpty())
    }

    @Test
    fun testConfigSetNonExistingKey() {
        Config.set("nonexistingkey", "x")

        assertEquals(1, Notifications.list.size)
        assertEquals("Could not set configuration setting: nonexistingkey (nonexistingkey)", Notifications.list[0].message)
        assertNull(Config.get("nonexistingkey"))
    }

    @Test
    fun testEnableWriteToFileSetTrue() {
        PropertyLoader.writeToFile = false

        Config.enableWriteToFile(true)

        assertTrue(PropertyLoader.writeToFile)
    }

    @Test
    fun testEnableWriteToFileSetFalse() {
        PropertyLoader.writeToFile = true

        Config.enableWriteToFile(false)

        assertFalse(PropertyLoader.writeToFile)
    }
}