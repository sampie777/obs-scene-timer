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

    fun register(component: Refreshable) {
        components.add(component)
    }

    fun unregister(component: Refreshable) {
        components.remove(component)
    }
}