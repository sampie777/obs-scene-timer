package nl.sajansen.obsscenetimer.objects.websocketScanner

import nl.sajansen.obsscenetimer.gui.websocketScanner.WebsocketScannerFrame
import org.slf4j.LoggerFactory
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import javax.swing.SwingWorker

class WebsocketScannerSwingWorker(
    private val component: WebsocketScannerFrame,
    private val timeout: Int = 200
) : SwingWorker<Boolean, Void>(),
    PropertyChangeListener {
    private val logger = LoggerFactory.getLogger(WebsocketScannerSwingWorker::class.java.name)

    override fun doInBackground(): Boolean {
        val processStatus = WebsocketScannerProcessStatus(this)
        val websocketScanner = WebsocketScanner(processStatus, timeout)
        websocketScanner.scan()

        component.processScanStatus("Scan finished")
        component.onScanFinished()
        return true
    }

    override fun propertyChange(event: PropertyChangeEvent?) {
        if (event == null) {
            return
        }

        if (WebsocketScannerProcessStatus.STATUS_PROGRESS == event.propertyName) {
            component.processScanStatus(event.newValue as String)
        }

        if (WebsocketScannerProcessStatus.VALUE_PROGRESS == event.propertyName) {
            component.addScanResult(event.newValue as ScanResult)
        }
    }
}