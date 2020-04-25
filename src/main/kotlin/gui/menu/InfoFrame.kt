package gui.menu

import gui.utils.ClickableLinkComponent
import themes.Theme
import java.awt.Dimension
import java.awt.Font
import java.awt.Frame
import javax.swing.*
import javax.swing.border.EmptyBorder

class InfoFrame(private val parentFrame: Frame?) : JDialog(parentFrame) {

    init {
        createGui()
    }

    private fun createGui() {
        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.PAGE_AXIS)
        mainPanel.border = EmptyBorder(0, 20, 10, 20)
        add(mainPanel)

        val currentRelease = "1.2.0"
        val versionLabel = JLabel("<html><h1>OBS Scene Timer</h1><p>By Samuel-Anton Jansen</p><p>Version: $currentRelease</p></html>")
        versionLabel.font = Font(Theme.get.FONT_FAMILY, Font.PLAIN, 14)
        val sourceCodeLabel = ClickableLinkComponent(
            "OBS Scene Timer on BitBucket",
            "https://bitbucket.org/sajansen/obs-scene-timer/src/master/README.md"
        )
        sourceCodeLabel.font = Font(Theme.get.FONT_FAMILY, Font.PLAIN, 14)

        mainPanel.add(versionLabel)
        mainPanel.add(Box.createRigidArea(Dimension(0, 10)))
        mainPanel.add(sourceCodeLabel)

        title = "Information"
        setSize(400, 160)
        setLocationRelativeTo(parentFrame)
        modalityType = ModalityType.APPLICATION_MODAL
        isResizable = false
        isVisible = true
    }
}