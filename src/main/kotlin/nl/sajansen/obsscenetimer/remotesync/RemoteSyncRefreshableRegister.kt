package nl.sajansen.obsscenetimer.remotesync


import nl.sajansen.obsscenetimer.remotesync.objects.ConnectionState
import nl.sajansen.obsscenetimer.remotesync.objects.RemoteSyncRefreshable
import nl.sajansen.obsscenetimer.utils.Rollbar
import org.slf4j.LoggerFactory

object RemoteSyncRefreshableRegister {
    private val logger = LoggerFactory.getLogger(RemoteSyncRefreshableRegister::class.java.name)

    private val components: HashSet<RemoteSyncRefreshable> = HashSet()

    fun remoteSyncClientRefreshConnectionState(state: ConnectionState) {
        val componentsCopy = components.toTypedArray()
        for (component in componentsCopy) {
            try {
                component.remoteSyncClientRefreshConnectionState(state)
            } catch (t: Throwable) {
                logger.error("Failed to execute remoteSyncClientRefreshConnectionState() for component ${component.javaClass}. ${t.localizedMessage}")
                Rollbar.error(t, mapOf("state" to state), "Failed to execute remoteSyncClientRefreshConnectionState() for component ${component.javaClass}")
                t.printStackTrace()
            }
        }
    }

    fun remoteSyncServerRefreshConnectionState() {
        val componentsCopy = components.toTypedArray()
        for (component in componentsCopy) {
            try {
                component.remoteSyncServerRefreshConnectionState()
            } catch (t: Throwable) {
                logger.error("Failed to execute remoteSyncServerRefreshConnectionState() for component ${component.javaClass}. ${t.localizedMessage}")
                Rollbar.error(t, "Failed to execute remoteSyncServerRefreshConnectionState() for component ${component.javaClass}")
                t.printStackTrace()
            }
        }
    }

    fun remoteSyncServerConnectionsUpdate() {
        val componentsCopy = components.toTypedArray()
        for (component in componentsCopy) {
            try {
                component.remoteSyncServerConnectionsUpdate()
            } catch (t: Throwable) {
                logger.error("Failed to execute remoteSyncServerConnectionsUpdate() for component ${component.javaClass}. ${t.localizedMessage}")
                Rollbar.error(t, "Failed to execute remoteSyncServerConnectionsUpdate() for component ${component.javaClass}")
                t.printStackTrace()
            }
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