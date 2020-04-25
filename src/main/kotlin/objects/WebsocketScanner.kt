package objects

import java.net.*
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.logging.Logger


class ScanResult(val ip: String, val port: Int, val open: Boolean)


class WebsocketScanner {
    private val logger = Logger.getLogger(WebsocketScanner::class.java.name)

    private val defaultPort = 4444
    private val timeout = 200
    private val threadPoolSize = 20

    fun scan() {
        val localNetworkIpAddresses = getLocalNetworkIpAddresses()

        val scanResultFutures: ArrayList<Future<ScanResult>> = scanIpAddresses(localNetworkIpAddresses)

        val addressesFound = scanResultFutures
            .map { it.get() }
            .filter { it.open }

        if (addressesFound.isEmpty()) {
            logger.info("No open websockets found")
            return
        }

        addressesFound.forEach { logger.info("Found websocket on: ${it.ip}:${it.port}") }

    }

    private fun getLocalNetworkIpAddresses(): List<String> {
        logger.info("Getting network IP addresses from host")
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

        localNetworkIpAddresses.forEach {
            val ipTemplate = it.substringBeforeLast(".")
            logger.info("Scanning IP addresses for base IP: $ipTemplate.0")
            for (ip in 1..255) {
                scanResultFutures.add(scanAddressPort(es, "$ipTemplate.$ip", defaultPort, timeout))
            }
        }
        es.shutdown()
        return scanResultFutures
    }

    private fun scanAddressPort(es: ExecutorService, ip: String, port: Int, timeout: Int): Future<ScanResult> {
        return es.submit(Callable {
            try {
                logger.fine("Probing: $ip:$port")
                val socket = Socket()
                socket.connect(InetSocketAddress(ip, port), timeout)
                socket.close()
                return@Callable ScanResult(ip, port, true)
            } catch (ex: Exception) {
                return@Callable ScanResult(ip, port, false)
            }
        })
    }
}