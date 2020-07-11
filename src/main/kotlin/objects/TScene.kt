package objects

import java.util.*
import java.util.logging.Logger


class TScene {
    private val logger = Logger.getLogger(TScene::class.java.name)
    var name = ""
    var sources: List<TSource> = ArrayList()
    private val groups: MutableSet<Int> = mutableSetOf()

    constructor()

    constructor(name: String?) {
        this.name = name ?: ""
    }

    override fun toString(): String = name

    fun maxVideoLength(): Int {
        val longestVideoLengthSource = longestVideoLengthSource()
        if (!longestVideoLengthSource.isPresent) {
            logger.info("No longest video source found for TScene $name")
        } else {
            logger.info("Longest video source for TScene '" + name + "' has length = " + longestVideoLengthSource.get().videoLength)
        }
        return longestVideoLengthSource.map(TSource::videoLength).orElse(0)
    }

    private fun longestVideoLengthSource(): Optional<TSource> =
        sources.stream().max(Comparator.comparingInt(TSource::videoLength))

    fun addToGroup(groupNumber: Int) {
        logger.fine("Adding scene '$name' to group: $groupNumber")
        groups.add(groupNumber)
    }

    fun removeFromGroup(groupNumber: Int) {
        logger.fine("Removing scene '$name' from group: $groupNumber")
        groups.remove(groupNumber)
    }

    fun removeFromAllGroups() {
        logger.fine("Removing scene '$name' from all groups")
        groups.clear()
    }

    fun isInGroup(groupNumber: Int): Boolean {
        return groups.contains(groupNumber)
    }

    fun isInSameGroupAs(scene: TScene): Boolean {
        if (groups.size == 0 || scene.groups.size == 0) {
            return false
        }

        return groups.any { scene.isInGroup(it) }
    }

    fun setGroupsFrom(sourceScene: TScene) {
        groups.clear()
        groups.addAll(sourceScene.groups)
    }
}