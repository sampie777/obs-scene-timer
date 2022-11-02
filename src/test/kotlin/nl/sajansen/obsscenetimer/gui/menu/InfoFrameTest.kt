package nl.sajansen.obsscenetimer.gui.menu

import nl.sajansen.obsscenetimer.ApplicationInfo
import nl.sajansen.obsscenetimer.gui.utils.ClickableLinkComponent
import nl.sajansen.obsscenetimer.resetConfig
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class InfoFrameTest {

    @BeforeTest
    fun before() {
        resetConfig()
    }

    @Test
    fun testInfoFrameContents() {
        val dialog = InfoFrame.create(null)

        assertEquals("Information", dialog.title)

        val components = (dialog.contentPane.components[0] as JPanel).components
        assertEquals(7, components.size)

        val infoLabel = components[0] as JLabel
        val codeLink = components[2] as ClickableLinkComponent
        assertTrue(infoLabel.text.contains(ApplicationInfo.name))
        assertTrue(infoLabel.text.contains(ApplicationInfo.author))
        assertTrue(infoLabel.text.contains(ApplicationInfo.version))
        assertEquals("Source code (GitHub)", codeLink.text)
    }
}