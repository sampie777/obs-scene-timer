package utils

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class UtilsFilesTest {

    @Test
    fun testGetVideoLength() {
        val filename = File(javaClass.classLoader.getResource("video2seconds.mkv")!!.file).absolutePath

        assertEquals(2, getVideoLength(filename))
    }

    @Test
    fun testGetVideoLengthForNonExistingVideo() {
        val filename = "nonexistingfile"

        assertEquals(0, getVideoLength(filename))
    }

}