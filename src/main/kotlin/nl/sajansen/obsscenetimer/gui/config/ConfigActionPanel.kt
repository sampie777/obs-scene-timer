package nl.sajansen.obsscenetimer.gui.config

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.gui.mainFrame.MainFrame
import nl.sajansen.obsscenetimer.themes.Theme
import nl.sajansen.obsscenetimer.utils.Rollbar
import org.slf4j.LoggerFactory
import java.awt.Dimension
import java.awt.event.KeyEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class ConfigActionPanel(private val frame: ConfigFrame) : JPanel() {
    private val logger = LoggerFactory.getLogger(ConfigActionPanel::class.java.name)

    val saveButton = JButton("Save")

    init {
        createGui()
    }

    private fun createGui() {
        layout = BoxLayout(this, BoxLayout.LINE_AXIS)
        border = EmptyBorder(0, 10, 10, 10)

        saveButton.addActionListener { saveConfigAndClose() }
        saveButton.mnemonic = KeyEvent.VK_S
        frame.rootPane.defaultButton = saveButton

        val cancelButton = JButton("Cancel")
        cancelButton.addActionListener { cancelWindow() }
        cancelButton.mnemonic = KeyEvent.VK_C

        add(Box.createHorizontalGlue())
        add(saveButton)
        add(Box.createRigidArea(Dimension(10, 0)))
        add(cancelButton)
    }

    private fun cancelWindow() {
        logger.debug("Exiting configuration window")
        frame.dispose()
    }

    private fun saveConfigAndClose() {
        logger.info("Saving configuration changes")
        if (!frame.saveAll()) {
            return
        }

        Config.save()
        frame.dispose()
        Rollbar.enable(Config.enableAutomaticErrorReporting)
        Theme.init()
        MainFrame.getInstance()?.rebuildGui()
    }
}
