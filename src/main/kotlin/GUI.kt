import gui.Refreshable

object GUI {
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


    fun register(component: Refreshable) {
        components.add(component)
    }

    fun isRegistered(component: Refreshable): Boolean {
        return components.contains(component)
    }

    fun unregister(component: Refreshable) {
        components.remove(component)
    }

    fun unregisterAll() {
        components.clear()
    }
}