package nl.sajansen.obsscenetimer

import nl.sajansen.obsscenetimer.gui.Refreshable
import nl.sajansen.obsscenetimer.objects.TScene
import nl.sajansen.obsscenetimer.utils.Rollbar
import org.slf4j.LoggerFactory
import java.awt.Component

object GUI {
    private val logger = LoggerFactory.getLogger(GUI::class.java.name)

    private val components: HashSet<Refreshable> = HashSet()

    fun refreshTimer() {
        val componentsCopy = components.toTypedArray()
        for (component in componentsCopy) {
            try {
                component.refreshTimer()
            } catch (t: Throwable) {
                logger.error("Failed to execute refreshTimer() for component ${component.javaClass}. ${t.localizedMessage}")
                Rollbar.error(t, "Failed to execute refreshTimer() for component ${component.javaClass}")
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
                logger.error("Failed to execute switchedScenes() for component ${component.javaClass}. ${t.localizedMessage}")
                Rollbar.error(t, "Failed to execute switchedScenes() for component ${component.javaClass}")
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
                logger.error("Failed to execute refreshScenes() for component ${component.javaClass}. ${t.localizedMessage}")
                Rollbar.error(t, "Failed to execute refreshScenes() for component ${component.javaClass}")
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
                logger.error("Failed to execute refreshGroups() for component ${component.javaClass}. ${t.localizedMessage}")
                Rollbar.error(t, "Failed to execute refreshGroups() for component ${component.javaClass}")
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
                logger.error("Failed to execute onSceneTimeLimitUpdated() for component ${component.javaClass}. ${t.localizedMessage}")
                Rollbar.error(t, mapOf("scene" to scene), "Failed to execute onSceneTimeLimitUpdated() for component ${component.javaClass}")
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
                logger.error("Failed to execute refreshOBSStatus() for component ${component.javaClass}. ${t.localizedMessage}")
                Rollbar.error(t, "Failed to execute refreshOBSStatus() for component ${component.javaClass}")
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
                logger.error("Failed to execute refreshNotifications() for component ${component.javaClass}. ${t.localizedMessage}")
                Rollbar.error(t, "Failed to execute refreshNotifications() for component ${component.javaClass}")
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
                logger.error("Failed to execute window() for component ${component.javaClass}. ${t.localizedMessage}")
                Rollbar.error(t, "Failed to execute window() for component ${component.javaClass}")
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