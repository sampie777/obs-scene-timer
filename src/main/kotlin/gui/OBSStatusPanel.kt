package gui

import GUI
import config.Config
import config.PropertyLoader
import objects.Globals
import objects.OBSStatus
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
        messageLabel.toolTipText = settingsFileString()
        add(messageLabel)
    }

    fun getMessageLabel(): JLabel {
        return messageLabel
    }

    override fun refreshOBSStatus() {
        messageLabel.text = "OBS: ${getOBSStatusRepresentation()}"

        if (Globals.OBSConnectionStatus == OBSStatus.CONNECTED) {
            messageLabel.toolTipText = "Connected to ${Config.obsAddress}. ${settingsFileString()}"
        } else {
            messageLabel.toolTipText = settingsFileString()
        }
        repaint()
    }

    fun getOBSStatusRepresentation(): String {
        val obsDisplayStatus = if (Globals.OBSActivityStatus != null)
            Globals.OBSActivityStatus else Globals.OBSConnectionStatus

        var obsDisplayStatusString = obsDisplayStatus!!.status
        if (obsDisplayStatus == OBSStatus.CONNECTING) {
            obsDisplayStatusString = "Connecting to ${Config.obsAddress}..."
        } else if (obsDisplayStatus == OBSStatus.CONNECTION_FAILED && Globals.OBSConnectionFailedMessage.isNotEmpty()) {
            obsDisplayStatusString = "Connection failed: " + Globals.OBSConnectionFailedMessage
        }

        return obsDisplayStatusString
    }

    private fun settingsFileString(): String {
        return "Settings file: " + PropertyLoader.getPropertiesFile().absolutePath
    }
}