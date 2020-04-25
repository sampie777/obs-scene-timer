package gui.websocketScanner

import config.Config
import objects.websocketScanner.ScanResult
import java.awt.BorderLayout
import java.awt.Frame
import java.util.logging.Logger
import javax.swing.JDialog
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class WebsocketScannerFrame(private val parentFrame: Frame?) : JDialog(parentFrame) {
    private val logger = Logger.getLogger(WebsocketScannerFrame::class.java.name)

    private val websocketScannerTable = WebsocketScannerTable(this)
    private val websocketScannerStatusPanel = WebsocketScannerStatusPanel()

    init {
        createGui()
    }

    private fun createGui() {
        val mainPanel = JPanel(BorderLayout(10, 10))
        mainPanel.border = EmptyBorder(10, 10, 10, 10)
        add(mainPanel)

        val bottomPanel = JPanel(BorderLayout())
        bottomPanel.add(websocketScannerStatusPanel, BorderLayout.LINE_START)
        bottomPanel.add(WebsocketScannerActionPanel(this), BorderLayout.LINE_END)

        mainPanel.add(WebsocketScannerInfoPanel(), BorderLayout.PAGE_START)
        mainPanel.add(websocketScannerTable, BorderLayout.CENTER)
        mainPanel.add(bottomPanel, BorderLayout.PAGE_END)

        title = "Network Scanner"
        setSize(600, 520)
        setLocationRelativeTo(parentFrame)
        modalityType = ModalityType.APPLICATION_MODAL
        isVisible = true
    }

    fun scan() {
        websocketScannerTable.scan()
    }

    fun processScanStatus(status: String) {
        websocketScannerStatusPanel.updateStatus(status)
    }

    fun processScanResults(scanResults: List<ScanResult>) {
        websocketScannerTable.setScanResults(scanResults)
    }

    fun addScanResult(scanResult: ScanResult) {
        websocketScannerTable.addScanResult(scanResult)
    }

    fun save(): Boolean {
        val newAddress = websocketScannerTable.getSelectedValueAsAddress()
        if (newAddress == null) {
            logger.info("No valid value selected")
            JOptionPane.showMessageDialog(this, "No value selected to save. Please select a table row and try\n" +
                    "Save again, or use Cancel to exit this window.", "No selection", JOptionPane.WARNING_MESSAGE)
            return false
        }

        logger.info("Saving new obsAddress: $newAddress")
        Config.obsAddress = newAddress
        Config.save()
        return true
    }
}