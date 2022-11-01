package nl.sajansen.obsscenetimer.objects

import nl.sajansen.obsscenetimer.config.Config
import java.util.logging.Logger


class TScene {
    private val logger = Logger.getLogger(TScene::class.java.name)

    var name = ""
    var sources: ArrayList<TSource> = ArrayList()

    @Volatile
    var timeLimit: Int? = null
    val groups: MutableSet<Int> = mutableSetOf()

    var sourcesAreLoaded = false

    companion object {
        fun fromJson(jsonTScene: Json.TScene): TScene {
            return TScene(jsonTScene.name).apply {
                timeLimit = jsonTScene.timeLimit
                groups.addAll(jsonTScene.groups)
            }
        }
    }

    constructor()

    constructor(name: String?, timeLimit: Int? = null) {
        this.name = name ?: ""
        this.timeLimit = timeLimit
    }

    override fun toString(): String = name

    fun resetTimeLimit() {
        logger.info("Resetting scene's time limit")
        timeLimit = null
        Config.sceneProperties.tScenes.find { it.name == name }?.timeLimit = maxVideoLength()
    }

    fun maxVideoLength(): Int {
        val longestVideoLengthSource = longestVideoLengthSource()
        if (longestVideoLengthSource == null) {
            logger.info("No longest video source found for TScene $name")
        } else {
            logger.info("Longest video source for TScene '" + name + "' has length = " + longestVideoLengthSource.file?.duration)
        }
        return longestVideoLengthSource?.file?.duration ?: 0
    }

    private fun longestVideoLengthSource(): TSource? =
        sources.filter { it.file != null }
            .maxByOrNull { it.file?.duration ?: 0 }

    /**
     * Get the time limit to use for a scene. First get the user specified limit from the scene itself.
     * Else, get the previously user specified limit from the config (something is wrong if this happens to be the case)
     * Last, get the max video length, if calculated
     */
    fun getFinalTimeLimit(): Int {
        return timeLimit
            ?: Config.sceneProperties.tScenes.find { it.name == name }?.timeLimit
            ?: maxVideoLength()
    }

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

    fun setGroups(groups: Set<Int>) {
        this.groups.clear()
        this.groups.addAll(groups)
    }

    fun setGroupsFrom(sourceScene: TScene) {
        groups.clear()
        groups.addAll(sourceScene.groups)
    }

    fun toJson(): Json.TScene {
        return Json.TScene(
            name = name,
            timeLimit = timeLimit,
            groups = groups
        )
    }
}

class Json {
    data class TScenes(
        val tScenes: ArrayList<TScene>
    )

    data class TScene(
        val name: String,
        var timeLimit: Int?,
        val groups: Set<Int>
    )
}