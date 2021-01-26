package updater

import objects.ApplicationInfo
import org.junit.Test
import org.mockito.Mockito.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UpdateCheckerTest {
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
    fun `test isNewUpdateAvailable returns true when latest version is different from application version`() {
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

        assertTrue(updateChecker.isNewUpdateAvailable())
    }

    @Test
    fun `test isNewUpdateAvailable returns false when latest version is not different from application version`() {
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

        assertFalse(updateChecker.isNewUpdateAvailable())
    }
}