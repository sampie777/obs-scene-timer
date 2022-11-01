package mocks

import nl.sajansen.obsscenetimer.gui.websocketScanner.WebsocketScannerFrame
import nl.sajansen.obsscenetimer.objects.websocketScanner.ScanResult

class WebsocketScannerFrameMock : WebsocketScannerFrame(null, visible = false) {
    var isProcessScanStatusCalled = false
    var processScanStatusValue: String? = null
    var isAddScanResultCalled = false
    var addScanResultValue: ScanResult? = null

    override fun processScanStatus(status: String) {
        isProcessScanStatusCalled = true
        processScanStatusValue = status
    }

    override fun addScanResult(scanResult: ScanResult) {
        isAddScanResultCalled = true
        addScanResultValue = scanResult
    }
}