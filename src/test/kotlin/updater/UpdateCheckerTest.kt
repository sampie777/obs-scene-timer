package updater

import objects.ApplicationInfo
import objects.notifications.Notifications
import org.junit.Test
import org.mockito.Mockito.*
import java.net.MalformedURLException
import kotlin.test.*

class UpdateCheckerTest {

    @BeforeTest
    fun before() {
        Notifications.clear()
        UpdateChecker().updateLatestKnownVersion(ApplicationInfo.version)
    }

    @Test
    fun `test getRemoteTagResponse creates notification when URL is malformed and returns null`() {
        val urlMock = mock(wURL::class.java)
        val updateChecker = UpdateChecker(urlMock)
        `when`(urlMock.readText(anyString())).then { throw MalformedURLException("errormessage") }

        assertNull(updateChecker.getRemoteTagResponse())

        assertEquals(1, Notifications.unreadNotifications)
        assertEquals(
            "Failed to check for updates: invalid URL. Please inform the developer of this application. More detailed: errormessage.",
            Notifications.list.first().message
        )
    }

    @Test
    fun `test getRemoteTagResponse creates no notifications on another error and returns null`() {
        val urlMock = mock(wURL::class.java)
        val updateChecker = UpdateChecker(urlMock)
        `when`(urlMock.readText(anyString())).then { throw Exception("errormessage") }

        assertNull(updateChecker.getRemoteTagResponse())

        assertEquals(0, Notifications.unreadNotifications)
    }

    @Test
    fun `test getRemoteTags creates notification on JSON error and returns empty list`() {
        val urlMock = mock(wURL::class.java)
        val updateChecker = UpdateChecker(urlMock)
        `when`(urlMock.readText(anyString())).thenReturn("invalid json message")

        val list = updateChecker.getRemoteTags()

        assertTrue(list.isEmpty())
        assertEquals(1, Notifications.unreadNotifications)
        assertEquals(
            "Failed to check for updates: invalid JSON response. Please inform the developer of this application. More detailed: java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 1 path \$.",
            Notifications.list.first().message
        )
    }

    @Test
    fun `test getRemoteTags returns a list of tags from a JSON response`() {
        val urlMock = mock(wURL::class.java)
        val updateChecker = UpdateChecker(urlMock)
        `when`(urlMock.readText(anyString())).thenReturn(
            """
            {
              "pagelen": 3,
              "size": 11,
              "values": [
                {
                  "name": "v1.6.0",
                  "links": {},
                  "tagger": null,
                  "date": null,
                  "message": null,
                  "type": "tag",
                  "target": {}
                },
                {
                  "name": "v1.5.0",
                  "links": {},
                  "tagger": null,
                  "date": null,
                  "message": null,
                  "type": "tag",
                  "target": {}
                },
                {
                  "name": "v1.4.1-SNAPSHOT",
                  "links": {},
                  "tagger": null,
                  "date": null,
                  "message": null,
                  "type": "tag",
                  "target": {}
                }
              ],
              "page": 1,
              "next": "https://api.bitbucket.org/2.0/repositories/sajansen/obs-scene-timer/refs/tags?sort=-name&page=2"
            }
        """.trimIndent()
        )

        val versions = updateChecker.getRemoteTags()

        assertEquals(listOf("v1.6.0", "v1.5.0", "v1.4.1-SNAPSHOT"), versions)
    }

    @Test
    fun `test isNewUpdateAvailable returns true when latest version is different and unknown`() {
        val urlMock = mock(wURL::class.java)
        val updateChecker = UpdateChecker(urlMock)
        `when`(urlMock.readText(anyString())).thenReturn(
            """
            {
              "pagelen": 3,
              "size": 11,
              "values": [
                {
                  "name": "v999.6.0",
                  "links": {},
                  "tagger": null,
                  "date": null,
                  "message": null,
                  "type": "tag",
                  "target": {}
                },
                {
                  "name": "v1.5.0",
                  "links": {},
                  "tagger": null,
                  "date": null,
                  "message": null,
                  "type": "tag",
                  "target": {}
                },
                {
                  "name": "v1.4.1-SNAPSHOT",
                  "links": {},
                  "tagger": null,
                  "date": null,
                  "message": null,
                  "type": "tag",
                  "target": {}
                }
              ],
              "page": 1,
              "next": "https://api.bitbucket.org/2.0/repositories/sajansen/obs-scene-timer/refs/tags?sort=-name&page=2"
            }
        """.trimIndent()
        )
        updateChecker.updateLatestKnownVersion(ApplicationInfo.version)

        assertTrue(updateChecker.isNewUpdateAvailable())
        assertEquals("v999.6.0", updateChecker.getLatestKnownVersion())
    }

    @Test
    fun `test isNewUpdateAvailable returns false when latest version is different but already known`() {
        val urlMock = mock(wURL::class.java)
        val updateChecker = UpdateChecker(urlMock)
        `when`(urlMock.readText(anyString())).thenReturn(
            """
            {
              "pagelen": 3,
              "size": 11,
              "values": [
                {
                  "name": "v999.6.0",
                  "links": {},
                  "tagger": null,
                  "date": null,
                  "message": null,
                  "type": "tag",
                  "target": {}
                },
                {
                  "name": "v1.5.0",
                  "links": {},
                  "tagger": null,
                  "date": null,
                  "message": null,
                  "type": "tag",
                  "target": {}
                },
                {
                  "name": "v1.4.1-SNAPSHOT",
                  "links": {},
                  "tagger": null,
                  "date": null,
                  "message": null,
                  "type": "tag",
                  "target": {}
                }
              ],
              "page": 1,
              "next": "https://api.bitbucket.org/2.0/repositories/sajansen/obs-scene-timer/refs/tags?sort=-name&page=2"
            }
        """.trimIndent()
        )
        updateChecker.updateLatestKnownVersion("v999.6.0")

        assertFalse(updateChecker.isNewUpdateAvailable())
        assertEquals("v999.6.0", updateChecker.getLatestKnownVersion())
    }

    @Test
    fun `test isNewUpdateAvailable returns false when latest version is not different but unknown`() {
        val urlMock = mock(wURL::class.java)
        val updateChecker = UpdateChecker(urlMock)
        `when`(urlMock.readText(anyString())).thenReturn(
            """
            {
              "pagelen": 3,
              "size": 11,
              "values": [
                {
                  "name": "${ApplicationInfo.version}",
                  "links": {},
                  "tagger": null,
                  "date": null,
                  "message": null,
                  "type": "tag",
                  "target": {}
                },
                {
                  "name": "v1.5.0",
                  "links": {},
                  "tagger": null,
                  "date": null,
                  "message": null,
                  "type": "tag",
                  "target": {}
                },
                {
                  "name": "v1.4.1-SNAPSHOT",
                  "links": {},
                  "tagger": null,
                  "date": null,
                  "message": null,
                  "type": "tag",
                  "target": {}
                }
              ],
              "page": 1,
              "next": "https://api.bitbucket.org/2.0/repositories/sajansen/obs-scene-timer/refs/tags?sort=-name&page=2"
            }
        """.trimIndent()
        )
        updateChecker.updateLatestKnownVersion(ApplicationInfo.version)

        assertFalse(updateChecker.isNewUpdateAvailable())
        assertEquals(ApplicationInfo.version, updateChecker.getLatestKnownVersion())
    }

    @Test
    fun `test isNewUpdateAvailable returns false when latest version is not different and already known`() {
        val urlMock = mock(wURL::class.java)
        val updateChecker = UpdateChecker(urlMock)
        `when`(urlMock.readText(anyString())).thenReturn(
            """
            {
              "pagelen": 3,
              "size": 11,
              "values": [
                {
                  "name": "${ApplicationInfo.version}",
                  "links": {},
                  "tagger": null,
                  "date": null,
                  "message": null,
                  "type": "tag",
                  "target": {}
                },
                {
                  "name": "v1.5.0",
                  "links": {},
                  "tagger": null,
                  "date": null,
                  "message": null,
                  "type": "tag",
                  "target": {}
                },
                {
                  "name": "v1.4.1-SNAPSHOT",
                  "links": {},
                  "tagger": null,
                  "date": null,
                  "message": null,
                  "type": "tag",
                  "target": {}
                }
              ],
              "page": 1,
              "next": "https://api.bitbucket.org/2.0/repositories/sajansen/obs-scene-timer/refs/tags?sort=-name&page=2"
            }
        """.trimIndent()
        )
        updateChecker.updateLatestKnownVersion(ApplicationInfo.version)

        assertFalse(updateChecker.isNewUpdateAvailable())
        assertEquals(ApplicationInfo.version, updateChecker.getLatestKnownVersion())
    }

    @Test
    fun `test isNewUpdateAvailable returns false when API returns invalid data`() {
        val urlMock = mock(wURL::class.java)
        val updateChecker = UpdateChecker(urlMock)
        `when`(urlMock.readText(anyString())).thenReturn("invalid json message")
        val latestKnownVersion = updateChecker.getLatestKnownVersion()

        assertFalse(updateChecker.isNewUpdateAvailable())
        assertEquals(1, Notifications.unreadNotifications)
        assertTrue(Notifications.list.first().message.contains("Failed to check for updates"))
        // Latest known version doesn't change
        assertEquals(latestKnownVersion, updateChecker.getLatestKnownVersion())
    }
}