package nl.sajansen.obsscenetimer.remotesync


import nl.sajansen.obsscenetimer.remotesync.objects.ConnectionState
import nl.sajansen.obsscenetimer.remotesync.objects.RemoteSyncRefreshable
import java.util.logging.Logger

object RemoteSyncRefreshableRegister {
    private val logger = Logger.getLogger(RemoteSyncRefreshableRegister::class.java.name)

    private val components: HashSet<RemoteSyncRefreshable> = HashSet()

    fun remoteSyncClientRefreshConnectionState(state: ConnectionState) {
        val componentsCopy = components.toTypedArray()
        for (component in componentsCopy) {
            component.remoteSyncClientRefreshConnectionState(state)
        }
    }

    fun remoteSyncServerRefreshConnectionState() {
        val componentsCopy = components.toTypedArray()
        for (component in componentsCopy) {
            component.remoteSyncServerRefreshConnectionState()
        }
    }

    fun remoteSyncServerConnectionsUpdate() {
        val componentsCopy = components.toTypedArray()
        for (component in componentsCopy) {
            component.remoteSyncServerConnectionsUpdate()
        }
    }

    fun register(component: RemoteSyncRefreshable) {
        logger.info("Registering component: ${component::class.java}")
        components.add(component)
    }

    fun isRegistered(component: RemoteSyncRefreshable): Boolean {
        return components.contains(component)
    }

    fun unregister(component: RemoteSyncRefreshable) {
        logger.info("Unregistering component: ${component::class.java}")
        components.remove(component)
    }

    fun unregisterAll() {
        logger.info("Unregistering all (${components.size}) components")
        components.clear()
    }
}