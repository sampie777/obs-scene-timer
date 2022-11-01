package nl.sajansen.obsscenetimer.utils

import nl.sajansen.obsscenetimer.resetConfig
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class UtilsFilesTest {

    @Before
    fun before() {
        resetConfig()
    }

    @Test
    fun testGetVideoLength() {
        val filename = File(javaClass.classLoader.getResource("nl/sajansen/obsscenetimer/video2seconds.mkv")!!.file).absolutePath

        assertEquals(2, getVideoLength(filename))
    }

    @Test
    fun testGetVideoLengthForNonExistingVideo() {
        val filename = "nonexistingfile"

        assertEquals(0, getVideoLength(filename))
    }

}