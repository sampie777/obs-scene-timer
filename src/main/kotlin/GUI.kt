import gui.Refreshable
import java.util.logging.Logger

object GUI {
    private val logger = Logger.getLogger(GUI::class.java.name)

    private val components: HashSet<Refreshable> = HashSet()

    fun refreshTimer() {
        for (component in components) {
            component.refreshTimer()
        }
    }

    fun switchedScenes() {
        for (component in components) {
            component.switchedScenes()
        }
    }

    fun refreshScenes() {
        for (component in components) {
            component.refreshScenes()
        }
    }

    fun refreshOBSStatus() {
        for (component in components) {
            component.refreshOBSStatus()
        }
    }

    fun refreshNotifications() {
        for (component in components) {
            component.refreshNotifications()
        }
    }

    fun windowClosing() {
        for (component in components) {
            component.windowClosing()
        }
    }


    fun register(component: Refreshable) {
        logger.info("Registering component: ${component::class.java}")
        components.add(component)
    }

    fun isRegistered(component: Refreshable): Boolean {
        return components.contains(component)
    }

    fun unregister(component: Refreshable) {
        logger.info("Unregistering component: ${component::class.java}")
        components.remove(component)
    }

    fun unregisterAll() {
        logger.info("Unregistering all (${components.size}) components")
        components.clear()
    }
}