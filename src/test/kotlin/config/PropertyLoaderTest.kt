package config

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PropertyLoaderTest {

    @BeforeTest
    fun before() {
        ConfigMock.stringProperty1 = "stringValue1"
        ConfigMock.stringProperty2 = "stringValue2"
        ConfigMock.longProperty1 = 100
    }

    @Test
    fun testSavePropertiesForConfig() {
        PropertyLoader.saveConfig(ConfigMock::class.java)

        assertEquals("stringValue1", PropertyLoader.getUserProperties().getProperty("stringProperty1"))
        assertEquals("stringValue2", PropertyLoader.getUserProperties().getProperty("stringProperty2"))
        assertEquals("100", PropertyLoader.getUserProperties().getProperty("longProperty1"))
    }

    @Test
    fun testLoadPropertiesForConfig() {
        PropertyLoader.getUserProperties().setProperty("stringProperty1", "stringValue1.1")
        PropertyLoader.getUserProperties().setProperty("stringProperty2", "stringValue2.1")
        PropertyLoader.getUserProperties().setProperty("longProperty1", "200")

        PropertyLoader.loadConfig(ConfigMock::class.java)

        assertEquals("stringValue1.1", PropertyLoader.getUserProperties().getProperty("stringProperty1"))
        assertEquals("stringValue2.1", PropertyLoader.getUserProperties().getProperty("stringProperty2"))
        assertEquals("200", PropertyLoader.getUserProperties().getProperty("longProperty1"))
    }
}