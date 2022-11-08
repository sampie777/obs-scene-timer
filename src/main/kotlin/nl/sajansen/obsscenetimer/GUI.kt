package nl.sajansen.obsscenetimer

import nl.sajansen.obsscenetimer.gui.Refreshable
import nl.sajansen.obsscenetimer.objects.TScene
import nl.sajansen.obsscenetimer.utils.Rollbar
import org.slf4j.LoggerFactory
import runWithCatch
import java.awt.Component

object GUI {
    private val logger = LoggerFactory.getLogger(GUI::class.java.name)

    private val components: HashSet<Refreshable> = HashSet()

    fun refreshTimer() {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch({ component.refreshTimer() }, logger,
                logMessage = { "Failed to execute refreshTimer() for component ${component.javaClass}" })
        }
    }

    fun switchedScenes() {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch({ component.switchedScenes() }, logger,
                logMessage = { "Failed to execute switchedScenes() for component ${component.javaClass}" })
        }
    }

    fun refreshScenes() {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch({ component.refreshScenes() }, logger,
                logMessage = { "Failed to execute refreshScenes() for component ${component.javaClass}" })
        }
    }

    fun refreshGroups() {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch({ component.refreshGroups() }, logger,
                logMessage = { "Failed to execute refreshGroups() for component ${component.javaClass}" })
        }
    }

    fun onSceneTimeLimitUpdated(scene: TScene) {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch(
                { component.onSceneTimeLimitUpdated(scene) }, logger,
                logMessage = { "Failed to execute onSceneTimeLimitUpdated() for component ${component.javaClass}" },
                rollbarCustomObjects = mapOf("scene" to scene)
            )
        }
    }

    fun refreshOBSStatus() {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch({ component.refreshOBSStatus() }, logger,
                logMessage = { "Failed to execute refreshOBSStatus() for component ${component.javaClass}" })
        }
    }

    fun refreshNotifications() {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch({ component.refreshNotifications() }, logger,
                logMessage = { "Failed to execute refreshNotifications() for component ${component.javaClass}" })
        }
    }

    fun windowClosing(window: Component?) {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch({ component.windowClosing(window) }, logger,
                logMessage = { "Failed to execute window() for component ${component.javaClass}" })
        }
    }

    private fun cloneComponentsList(retries: Int = 6): Array<Refreshable> {
        return try {
            components.toTypedArray()
        } catch (t: Throwable) {
            if (retries > 0) {
                logger.info("Retrying clone ($retries)...")
                return cloneComponentsList(retries - 1)
            }

            logger.error("Failed to clone ${components.size} Refreshable components" + ((if (t.localizedMessage == null) "" else ". ${t.localizedMessage}")))
            Rollbar.error(t, mapOf("size" to components.size), "Failed to clone ${components.size} Refreshable components")
            t.printStackTrace()
            emptyArray()
        }
    }

    fun register(component: Refreshable) {
        logger.debug("Registering component: ${component::class.java}")
        components.add(component)
    }

    fun isRegistered(component: Refreshable): Boolean {
        return components.contains(component)
    }

    fun unregister(component: Refreshable) {
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

    fun registeredComponents() = components
}