package gui.config

import gui.utils.DefaultDialogKeyDispatcher
import java.awt.BorderLayout
import java.awt.KeyboardFocusManager
import java.util.logging.Logger
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JPanel

class ConfigFrame(internal val parentFrame: JFrame?) : JDialog(parentFrame) {
    private val logger = Logger.getLogger(ConfigFrame::class.java.name)

    private val configEditPanel: ConfigEditPanel = ConfigEditPanel()

    init {
        KeyboardFocusManager
            .getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(DefaultDialogKeyDispatcher(this))

        createGui()
    }

    private fun createGui() {
        val mainPanel = JPanel(BorderLayout(10, 10))
        add(mainPanel)

        val configActionPanel = ConfigActionPanel(this)

        mainPanel.add(configEditPanel, BorderLayout.CENTER)
        mainPanel.add(configActionPanel, BorderLayout.PAGE_END)

        pack()  // Realize components so the focus request will work
        configActionPanel.saveButton.requestFocusInWindow()

        title = "Settings"
        setSize(600, 520)
        setLocationRelativeTo(parentFrame)
        modalityType = ModalityType.APPLICATION_MODAL
        isVisible = true
    }

    fun saveAll(): Boolean {
        return configEditPanel.saveAll()
    }
}