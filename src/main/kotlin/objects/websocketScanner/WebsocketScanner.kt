package objects.websocketScanner

import java.net.*
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.logging.Logger


class ScanResult(
    val ip: String,
    val port: Int,
    val open: Boolean,
    val hostName: String? = null
) {
    override fun toString(): String = "${ip}:${port} (${hostName})"
}


class WebsocketScanner(private val processStatus: WebsocketScannerProcessStatus, private val port: Int = 4444) {
    private val logger = Logger.getLogger(WebsocketScanner::class.java.name)

    private val timeout = 200
    private val threadPoolSize = 20

    fun scan(): List<ScanResult> {
        val localNetworkIpAddresses = getLocalNetworkIpAddresses()

        val scanResultFutures: ArrayList<Future<ScanResult>> = scanIpAddresses(localNetworkIpAddresses)

        processStatus.setState("Filtering scan results")
        val addressesFound = scanResultFutures
            .map { it.get() }
            .filter { it.open }

        if (addressesFound.isEmpty()) {
            logger.info("No open websockets found")
            return addressesFound
        }

        return addressesFound
    }

    private fun getLocalNetworkIpAddresses(): List<String> {
        logger.info("Getting network IP addresses from host")
        processStatus.setState("Getting network IP addresses")
        val localNetworkIpAddresses = ArrayList<String>()

        NetworkInterface.getNetworkInterfaces().iterator()
            .forEach { networkInterface ->
                networkInterface.inetAddresses.iterator().forEach {
                    if (it.hostAddress.startsWith("192.168.")) {
                        localNetworkIpAddresses.add(it.hostAddress)
                    }
                }
            }

        return localNetworkIpAddresses
    }

    private fun scanIpAddresses(localNetworkIpAddresses: List<String>): ArrayList<Future<ScanResult>> {
        val es: ExecutorService = Executors.newFixedThreadPool(threadPoolSize)
        val scanResultFutures: ArrayList<Future<ScanResult>> = ArrayList()

        // Try localhost first
        scanResultFutures.add(scanAddressPort(es, "localhost", port, timeout))

        // Try IP addresses
        localNetworkIpAddresses.forEach {
            val ipTemplate = it.substringBeforeLast(".")
            logger.info("Scanning IP addresses for base IP: $ipTemplate.0")
            for (ip in 1..255) {
                scanResultFutures.add(scanAddressPort(es, "$ipTemplate.$ip", port, timeout))
            }
        }
        es.shutdown()
        return scanResultFutures
    }

    private fun scanAddressPort(es: ExecutorService, ip: String, port: Int, timeout: Int): Future<ScanResult> {
        return es.submit(Callable {
            try {
                logger.fine("Probing: $ip:$port")
                processStatus.setState("Probing: $ip:$port")

                val socket = Socket()
                val inetSocketAddress = InetSocketAddress(ip, port)
                socket.connect(inetSocketAddress, timeout)
                socket.close()

                val scanResult = ScanResult(ip, port, true, inetSocketAddress.hostName)
                logger.info("Found websocket on: $scanResult")
                processStatus.addScanResult(scanResult)

                return@Callable scanResult
            } catch (ex: Exception) {
                return@Callable ScanResult(ip, port, false)
            }
        })
    }
}