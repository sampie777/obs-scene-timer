package nl.sajansen.obsscenetimer.remotesync


import nl.sajansen.obsscenetimer.remotesync.objects.ConnectionState
import nl.sajansen.obsscenetimer.remotesync.objects.RemoteSyncRefreshable
import org.slf4j.LoggerFactory
import runWithCatch

object RemoteSyncRefreshableRegister {
    private val logger = LoggerFactory.getLogger(RemoteSyncRefreshableRegister::class.java.name)

    private val components: HashSet<RemoteSyncRefreshable> = HashSet()

    fun remoteSyncClientRefreshConnectionState(state: ConnectionState) {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch(
                { component.remoteSyncClientRefreshConnectionState(state) }, logger,
                logMessage = { "Failed to execute remoteSyncClientRefreshConnectionState() for component ${component.javaClass}" },
                rollbarCustomObjects = mapOf("state" to state)
            )
        }
    }

    fun remoteSyncServerRefreshConnectionState() {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch({ component.remoteSyncServerRefreshConnectionState() }, logger,
                logMessage = { "Failed to execute remoteSyncServerRefreshConnectionState() for component ${component.javaClass}" })
        }
    }

    fun remoteSyncServerConnectionsUpdate() {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch({ component.remoteSyncServerConnectionsUpdate() }, logger,
                logMessage = { "Failed to execute remoteSyncServerConnectionsUpdate() for component ${component.javaClass}" })
        }
    }

    private fun cloneComponentsList(): Array<RemoteSyncRefreshable> {
        return runWithCatch(
            { components.toTypedArray() }, logger,
            logMessage = { "Failed to clone ${components.size} RemoteSyncRefreshable components" },
            defaultReturnValue = emptyArray(),
            rollbarCustomObjects = mapOf("size" to components.size)
        )!!
    }

    fun register(component: RemoteSyncRefreshable) {
        logger.info("Registering component: ${component::class.java}")
        components.add(component)
    }

    fun isRegistered(component: RemoteSyncRefreshable): Boolean {
        return components.contains(component)
    }

    fun unregister(component: RemoteSyncRefreshable) {
        logger.debug("Unregistering component: ${component::class.java}")
        runWithCatch(
            { components.remove(component) }, logger,
            logMessage = { "Failed to unregister component ${component.javaClass}" },
            rollbarCustomObjects = mapOf("size" to components.size)
        )
    }

    fun unregisterAll() {
        logger.info("Unregistering all (${components.size}) components")
        components.clear()
    }
}