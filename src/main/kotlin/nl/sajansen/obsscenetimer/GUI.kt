package nl.sajansen.obsscenetimer

import nl.sajansen.obsscenetimer.gui.Refreshable
import nl.sajansen.obsscenetimer.objects.TScene
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
                logMessage = { "Failed to execute refreshTimer() for component ${component.javaClass}. ${it.localizedMessage}" })
        }
    }

    fun switchedScenes() {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch({ component.switchedScenes() }, logger,
                logMessage = { "Failed to execute switchedScenes() for component ${component.javaClass}. ${it.localizedMessage}" })
        }
    }

    fun refreshScenes() {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch({ component.refreshScenes() }, logger,
                logMessage = { "Failed to execute refreshScenes() for component ${component.javaClass}. ${it.localizedMessage}" })
        }
    }

    fun refreshGroups() {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch({ component.refreshGroups() }, logger,
                logMessage = { "Failed to execute refreshGroups() for component ${component.javaClass}. ${it.localizedMessage}" })
        }
    }

    fun onSceneTimeLimitUpdated(scene: TScene) {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch(
                { component.onSceneTimeLimitUpdated(scene) }, logger,
                logMessage = { "Failed to execute onSceneTimeLimitUpdated() for component ${component.javaClass}. ${it.localizedMessage}" },
                rollbarCustomObjects = mapOf("scene" to scene)
            )
        }
    }

    fun refreshOBSStatus() {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch({ component.refreshOBSStatus() }, logger,
                logMessage = { "Failed to execute refreshOBSStatus() for component ${component.javaClass}. ${it.localizedMessage}" })
        }
    }

    fun refreshNotifications() {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch({ component.refreshNotifications() }, logger,
                logMessage = { "Failed to execute refreshNotifications() for component ${component.javaClass}. ${it.localizedMessage}" })
        }
    }

    fun windowClosing(window: Component?) {
        val componentsCopy = cloneComponentsList()
        for (component in componentsCopy) {
            runWithCatch({ component.windowClosing(window) }, logger,
                logMessage = { "Failed to execute window() for component ${component.javaClass}. ${it.localizedMessage}" })
        }
    }

    private fun cloneComponentsList(): Array<Refreshable> {
        return runWithCatch(
            { components.toTypedArray() }, logger,
            logMessage = { "Failed to clone Refreshable components. ${it.localizedMessage}" },
            defaultReturnValue = emptyArray(),
            rollbarCustomObjects = mapOf("size" to components.size)
        )!!
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