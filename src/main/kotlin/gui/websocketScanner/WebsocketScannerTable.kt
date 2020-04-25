package gui.websocketScanner

import objects.websocketScanner.ScanResult
import objects.websocketScanner.WebsocketScannerSwingWorker
import java.awt.BorderLayout
import java.util.*
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

class WebsocketScannerTable(private val frame: WebsocketScannerFrame) : JPanel() {

    private val tableHeader = arrayOf("Name", "Address", "Port")
    private val table = JTable(ReadOnlyTableModel(tableHeader, 0))

    init {
        createGui()
    }

    private fun createGui() {
        layout = BorderLayout()

        add(JScrollPane(table), BorderLayout.CENTER)
    }

    private fun clearTable() {
        (table.model as DefaultTableModel).rowCount = 0
    }

    fun scan() {
        clearTable()

        val worker = WebsocketScannerSwingWorker(frame)
        worker.execute()
    }

    fun setScanResults(scanResults: List<ScanResult>) {
        clearTable()

        scanResults.forEach {
            addScanResult(it)
        }
    }

    fun addScanResult(scanResult: ScanResult) {
        (table.model as DefaultTableModel).addRow(arrayOf(scanResult.hostName, scanResult.ip, scanResult.port))
    }

    fun getSelectedValueAsAddress(): String? {
        val row: Vector<Any>?
        try {
            row = (table.model as DefaultTableModel).dataVector[table.selectedRow] as? Vector<Any>
        } catch (e: ArrayIndexOutOfBoundsException) {
            return null
        }

        if (row == null || row.size < 3) {
            return null
        }

        return "ws://${row[1]}:${row[2]}"
    }
}