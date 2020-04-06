package gui

import GUI
import config.PropertyLoader
import objects.Globals
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class OBSStatusPanel : JPanel(), Refreshable {

    private val messageLabel: JLabel = JLabel()

    init {
        GUI.register(this)
        initGUI()
        refreshOBSStatus()
    }

    private fun initGUI() {
        layout = BorderLayout(15, 15)
        border = EmptyBorder(10, 10, 10, 10)

        messageLabel.font = Font("Dialog", Font.PLAIN, 14)
        messageLabel.toolTipText = "Settings file: " + PropertyLoader.getPropertiesFile().absolutePath
        add(messageLabel)
    }

    override fun refreshOBSStatus() {
        messageLabel.text = "OBS: ${Globals.OBSStatus}"
        repaint()
    }
}