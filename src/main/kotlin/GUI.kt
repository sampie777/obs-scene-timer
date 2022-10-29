import gui.Refreshable
import objects.TScene
import java.awt.Component
import java.util.logging.Logger

object GUI {
    private val logger = Logger.getLogger(GUI::class.java.name)

    private val components: HashSet<Refreshable> = HashSet()

    fun refreshTimer() {
        val componentsCopy = components.toTypedArray()
        for (component in componentsCopy) {
            try {
                component.refreshTimer()
            } catch (t: Throwable) {
                logger.severe("Failed to execute refreshTimer() for component ${component.javaClass}")
                t.printStackTrace()
            }
        }
    }

    fun switchedScenes() {
        val componentsCopy = components.toTypedArray()
        for (component in componentsCopy) {
            try {
                component.switchedScenes()
            } catch (t: Throwable) {
                logger.severe("Failed to execute switchedScenes() for component ${component.javaClass}")
                t.printStackTrace()
            }
        }
    }

    fun refreshScenes() {
        val componentsCopy = components.toTypedArray()
        for (component in componentsCopy) {
            try {
                component.refreshScenes()
            } catch (t: Throwable) {
                logger.severe("Failed to execute refreshScenes() for component ${component.javaClass}")
                t.printStackTrace()
            }
        }
    }

    fun refreshGroups() {
        val componentsCopy = components.toTypedArray()
        for (component in componentsCopy) {
            try {
                component.refreshGroups()
            } catch (t: Throwable) {
                logger.severe("Failed to execute refreshGroups() for component ${component.javaClass}")
                t.printStackTrace()
            }
        }
    }

    fun onSceneTimeLimitUpdated(scene: TScene) {
        val componentsCopy = components.toTypedArray()
        for (component in componentsCopy) {
            try {
                component.onSceneTimeLimitUpdated(scene)
            } catch (t: Throwable) {
                logger.severe("Failed to execute onSceneTimeLimitUpdated() for component ${component.javaClass}")
                t.printStackTrace()
            }
        }
    }

    fun refreshOBSStatus() {
        val componentsCopy = components.toTypedArray()
        for (component in componentsCopy) {
            try {
                component.refreshOBSStatus()
            } catch (t: Throwable) {
                logger.severe("Failed to execute refreshOBSStatus() for component ${component.javaClass}")
                t.printStackTrace()
            }
        }
    }

    fun refreshNotifications() {
        val componentsCopy = components.toTypedArray()
        for (component in componentsCopy) {
            try {
                component.refreshNotifications()
            } catch (t: Throwable) {
                logger.severe("Failed to execute refreshNotifications() for component ${component.javaClass}")
                t.printStackTrace()
            }
        }
    }

    fun windowClosing(window: Component?) {
        val componentsCopy = components.toTypedArray()
        for (component in componentsCopy) {
            try {
                component.windowClosing(window)
            } catch (t: Throwable) {
                logger.severe("Failed to execute window() for component ${component.javaClass}")
                t.printStackTrace()
            }
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

    fun registeredComponents() = components
}