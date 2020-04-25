package objects.websocketScanner

import gui.websocketScanner.WebsocketScannerFrame

class WebsocketScannerFrameMock : WebsocketScannerFrame(null, visible = false) {
    var isProcessScanStatusCalled = false
    var processScanStatusValue: String? = null
    var isAddScanResultCalled = false
    var addScanResultValue: ScanResult? = null

    override fun processScanStatus(value: String) {
        isProcessScanStatusCalled = true
        processScanStatusValue = value
    }

    override fun addScanResult(scanResult: ScanResult) {
        isAddScanResultCalled = true
        addScanResultValue = scanResult
    }
}