package objects

import config.Config
import java.util.*
import java.util.logging.Logger


class TScene {
    private val logger = Logger.getLogger(TScene::class.java.name)
    var name = ""
    var sources: List<TSource> = ArrayList()
    var timeLimit: Int? = null
    private val groups: MutableSet<Int> = mutableSetOf()

    companion object {
        fun fromJson(jsonTScene: Json.TScene): TScene {
            if (jsonTScene.timeLimit == null) {
                Config.sceneLimitValues.remove(jsonTScene.name)
            } else {
                Config.sceneLimitValues[jsonTScene.name] = jsonTScene.timeLimit
            }

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
        val timeLimit: Int?,
        val groups: Set<Int>
    )
}