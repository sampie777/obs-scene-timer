package gui.updater

import objects.ApplicationInfo
import objects.notifications.Notifications
import java.awt.Desktop
import java.awt.Dimension
import java.awt.event.KeyEvent
import java.io.IOException
import java.net.URI
import java.util.logging.Logger
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class UpdatePopupActionPanel(private val frame: UpdatePopup) : JPanel() {
    private val logger = Logger.getLogger(UpdatePopupActionPanel::class.java.name)

    init {
        createGui()
    }

    private fun createGui() {
        layout = BoxLayout(this, BoxLayout.LINE_AXIS)
        border = EmptyBorder(0, 10, 10, 10)

        val closeButton = JButton("Close")
        closeButton.addActionListener { closePopup() }
        closeButton.mnemonic = KeyEvent.VK_C

        val downloadButton = JButton("Download")
        downloadButton.toolTipText = "The download website will be opened"
        downloadButton.addActionListener { downloadUpdate() }
        downloadButton.mnemonic = KeyEvent.VK_D
        frame.rootPane.defaultButton = downloadButton

        add(Box.createHorizontalGlue())
        add(closeButton)
        add(Box.createRigidArea(Dimension(10, 0)))
        add(downloadButton)
    }

    private fun closePopup() {
        logger.info("Closing update window")

        frame.dispose()
    }

    private fun downloadUpdate() {
        logger.info("Opening update download website")

        val url = ApplicationInfo.downloadsUrl
        if (!Desktop.isDesktopSupported()) {
            logger.warning("Cannot open link '$url': not supported by host")
        }
        try {
            Desktop.getDesktop().browse(URI(url))
        } catch (e: IOException) {
            logger.severe("Error during opening link '$url'")
            e.printStackTrace()
            Notifications.popup("Failed to open link: $url", "Download update")
        }

        closePopup()
    }
}
