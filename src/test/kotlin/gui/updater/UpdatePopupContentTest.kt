package gui.updater

import gui.utils.ClickableLinkComponent
import javax.swing.JLabel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class UpdatePopupContentTest {

    @Test
    fun testInfoFrameContents() {
        val contentPanel = UpdatePopupContent("v1.2.3")

        assertEquals(3, contentPanel.componentCount)

        val infoLabel = contentPanel.components[0] as JLabel
        assertTrue(infoLabel.text.contains("v1.2.3"))
        assertTrue(infoLabel.text.contains("new version is available"))

        val releaseNotesLink = contentPanel.components[2] as ClickableLinkComponent
        assertTrue(releaseNotesLink.text == "Release notes")
    }
}