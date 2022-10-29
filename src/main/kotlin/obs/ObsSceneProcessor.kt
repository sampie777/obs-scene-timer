package obs

import GUI
import com.google.gson.JsonArray
import config.Config
import io.obswebsocket.community.client.message.response.inputs.GetInputSettingsResponse
import io.obswebsocket.community.client.message.response.sceneitems.GetSceneItemListResponse
import io.obswebsocket.community.client.message.response.scenes.GetCurrentProgramSceneResponse
import io.obswebsocket.community.client.message.response.scenes.GetSceneListResponse
import io.obswebsocket.community.client.model.Scene
import io.obswebsocket.community.client.model.SceneItem
import isAddressLocalhost
import objects.*
import objects.notifications.Notifications
import utils.getVideoLengthOrZeroForFile
import java.util.logging.Logger

object ObsSceneProcessor {
    private var logger = Logger.getLogger(ObsSceneProcessor::class.java.name)

    private val videoSources = listOf("ffmpeg_source", "vlc_source")

    /**
     * Actively request the current scene from BOS
     */
    fun getCurrentSceneFromOBS() {
        logger.fine("Retrieving current scene")
        OBSClient.getController()?.getCurrentProgramScene { response: GetCurrentProgramSceneResponse ->
            if (OBSState.currentScene.name == response.currentProgramSceneName) {
                return@getCurrentProgramScene
            }

            try {
                processNewScene(response.currentProgramSceneName)
            } catch (t: Throwable) {
                logger.severe("Could not process current scene")
                t.printStackTrace()
                Notifications.add("Could not process current scene: ${t.localizedMessage}", "OBS")
            }
        }
    }

    /**
     * Set the new scene name as new current scene and notify everyone of this change
     */
    fun processNewScene(sceneName: String) {
        logger.info("New scene: $sceneName")

        val newScene: TScene = OBSState.scenes.find { it.name == sceneName } ?: run {
            logger.warning("New scene is not found in scene list. Creating new scene for it.")
            TScene(sceneName)
        }

        val isSameGroup = newScene.isInSameGroupAs(OBSState.currentScene)
        OBSState.currentScene = newScene

        if (!isSameGroup) {
            OBSSceneTimer.reset()
        }

        GUI.switchedScenes()
        GUI.refreshTimer()

        SceneLogger.log(OBSState.currentScene.name)
    }

    fun loadScenes() {
        logger.info("Retrieving scenes")
        OBSState.clientActivityStatus = OBSClientStatus.LOADING_SCENES
        GUI.refreshOBSStatus()

        try {
            OBSClient.getController()!!.getSceneList { response: GetSceneListResponse ->
                logger.info("${response.scenes.size} scenes retrieved")

                try {
                    processOBSScenesToOBSStateScenes(if (Config.reverseSceneOrder) response.scenes else response.scenes.reversed())
                } catch (t: Throwable) {
                    logger.severe("Failed to process scenes")
                    t.printStackTrace()
                    Notifications.add("Something went wrong during scenes processing: ${t.localizedMessage}", "OBS")
                    refreshGuiWithNewScenes()
                }
            }
        } catch (t: Throwable) {
            logger.severe("Failed to retrieve scenes")
            t.printStackTrace()
            Notifications.add("Something went wrong during retrieving scenes: ${t.localizedMessage}", "OBS")
            refreshGuiWithNewScenes()
        }
    }

    fun processOBSScenesToOBSStateScenes(scenes: List<Scene>) {
        logger.info("Set the OBS Scenes")

        @Suppress("UNCHECKED_CAST")
        val oldScenes: List<TScene> = OBSState.scenes.clone() as List<TScene>

        OBSState.scenes.clear()
        for (scene in scenes) {
            val tScene = TScene(scene.sceneName)

            OBSState.scenes.add(tScene)

            // Reassign pointer from currentScene to scene in new scenes list
            if (OBSState.currentScene.name == tScene.name) {
                OBSState.currentScene = tScene
            }

            // Copy old scene properties to new scene, if available
            oldScenes.find { it.name == tScene.name }
                ?.let { oldScene ->
                    tScene.timeLimit = oldScene.timeLimit
                    tScene.setGroupsFrom(oldScene)
                }
            // Else (if not available), copy properties from config, if available
                ?: Config.sceneProperties.tScenes.find { it.name == tScene.name }
                    ?.let { configScene ->
                        tScene.timeLimit = configScene.timeLimit
                        tScene.setGroups(configScene.groups)
                    }
        }

        loadSceneItems { loadSourceSettingsForAllScenes(callback = ::refreshGuiWithNewScenes) }
    }

    private fun loadSceneItems(callback: (() -> Unit)? = null) {
        if (!isAddressLocalhost(Config.obsAddress)) {
            logger.info("Not going to try to get the video lengths, because the source files are probably running on another computer")
            try {
                callback?.invoke()
            } catch (t: Throwable) {
                logger.severe("Failed to invoke callback when skipping loadSceneItems")
                t.printStackTrace()
            }
            return
        }

        val sceneToLoad = try {
            OBSState.scenes.firstOrNull { !it.sourcesAreLoaded }
        } catch (t: Throwable) {
            logger.severe("Failed to get next scene for loading scene items")
            t.printStackTrace()
            Notifications.add(
                "Something went wrong while loading scene items: ${t.localizedMessage}",
                "OBS"
            )
            null
        }

        if (sceneToLoad == null) {
            try {
                callback?.invoke()
            } catch (t: Throwable) {
                logger.severe("Failed to invoke callback during loadSceneItems")
                t.printStackTrace()
                Notifications.add(
                    "Something went wrong after loading scene items: ${t.localizedMessage}",
                    "OBS"
                )
            }
            return
        }

        try {
            loadItemsForScene(sceneToLoad) { loadSceneItems(callback) }
        } catch (t: Throwable) {
            logger.severe("Failed to load scene items for scene '${sceneToLoad.name}'")
            t.printStackTrace()
            Notifications.add(
                "Something went wrong while loading scene items for scene '${sceneToLoad.name}': ${t.localizedMessage}",
                "OBS"
            )
        }
    }

    fun loadItemsForScene(scene: TScene, callback: (() -> Unit)? = null) {
        OBSClient.getController()?.getSceneItemList(scene.name) { response: GetSceneItemListResponse ->
            response.sceneItems.forEach { item ->
                if (item.sourceType == "OBS_SOURCE_TYPE_INPUT" && videoSources.contains(item.inputKind)) {
                    addSceneItemToTScene(scene, item)
                }
//                else if (item.isGroup) {
//                    OBSClient.getSourcesForGroup(scene, item)
//                }
            }
            scene.sourcesAreLoaded = true

            try {
                callback?.invoke()
            } catch (t: Throwable) {
                logger.severe("Failed to invoke callback after loading scene items for scene '${scene.name}'")
                t.printStackTrace()
                Notifications.add(
                    "Something went wrong while loading scene items: ${t.localizedMessage}",
                    "OBS"
                )
            }
        }
    }

    private fun addSceneItemToTScene(scene: TScene, item: SceneItem) {
        logger.info("Adding item '${item.sourceName}' to scene: '${scene.name}'")
        scene.sources.add(TSource(name = item.sourceName, kind = item.inputKind))
    }

    fun loadSourceSettingsForAllScenes(forceAutoCalculation: Boolean = false, callback: (() -> Unit)? = null): Boolean {
        if (!forceAutoCalculation && !Config.autoCalculateSceneLimitsBySources) {
            logger.info("Auto calculation of scene time limits by source files is disabled")
            try {
                callback?.invoke()
            } catch (t: Throwable) {
                logger.severe("Failed to invoke callback when skipping loadSourceSettingsForAllScenes")
                t.printStackTrace()
            }
            return false
        }

        if (!isAddressLocalhost(Config.obsAddress)) {
            logger.info("Not going to try to get the video lengths, because the source files are probably running on another computer")
            try {
                callback?.invoke()
            } catch (t: Throwable) {
                logger.severe("Failed to invoke callback when skipping loadSourceSettingsForAllScenes")
                t.printStackTrace()
            }
            return false
        }

        OBSState.clientActivityStatus = OBSClientStatus.LOADING_SCENE_SOURCES
        GUI.refreshOBSStatus()

        val sourceToLoad = try {
            OBSState.scenes
                .flatMap(TScene::sources)
                .firstOrNull { !it.settingsLoaded }
        } catch (t: Throwable) {
            logger.severe("Failed to get next source for loading source settings")
            t.printStackTrace()
            Notifications.add(
                "Something went wrong while loading scene items source settings: ${t.localizedMessage}",
                "OBS"
            )
            null
        }

        if (sourceToLoad == null) {
            try {
                callback?.invoke()
            } catch (t: Throwable) {
                logger.severe("Failed to invoke callback during loadSourceSettingsForAllScenes")
                t.printStackTrace()
                Notifications.add(
                    "Something went wrong after loading scene item sources: ${t.localizedMessage}",
                    "OBS"
                )
            }
            return true
        }

        try {
            loadSourceSettingsForSource(sourceToLoad) { loadSourceSettingsForAllScenes(callback = callback) }
        } catch (t: Throwable) {
            logger.severe("Failed to load scene item sources settings for item '${sourceToLoad.name}'")
            t.printStackTrace()
            Notifications.add(
                "Something went wrong while processing scene item sources settings for item '${sourceToLoad.name}': ${t.localizedMessage}",
                "OBS"
            )
            callback?.invoke()
        }
        return true
    }

    private fun loadSourceSettingsForSource(source: TSource, callback: (() -> Unit)? = null) {
        // First check our scenes for matching sources
        val existingSourceFound = OBSState.scenes.any { scene ->
            val sameSource = scene.sources.find { it != source && it.name == source.name }
                ?: return@any false

            logger.info("Copying existing settings from matching source '${source.name}'.")
            sameSource.copyTo(source)
            source.settingsLoaded = true
            true
        }

        if (existingSourceFound) {
            try {
                callback?.invoke()
            } catch (t: Throwable) {
                logger.severe("Failed to invoke callback after copying source settings for source '${source.name}'")
                t.printStackTrace()
                Notifications.add(
                    "Something went wrong while loading scene items source settings: ${t.localizedMessage}",
                    "OBS"
                )
            }
            return
        }

        OBSClient.getController()?.getInputSettings(source.name) { response: GetInputSettingsResponse ->
            processGetInputSettingsResponse(source, response, callback)
        }
    }

    fun processGetInputSettingsResponse(source: TSource, response: GetInputSettingsResponse, callback: (() -> Unit)? = null) {
        logger.info("Processing received source settings for source '${source.name}'.")

        if (response.inputSettings.has("local_file")) {
            source.file = TVideoFile(name = response.inputSettings["local_file"].asString)
        }
        if (response.inputSettings.has("playlist")) {
            try {
                source.playlist = responsePlaylistToTPlaylist(response.inputSettings["playlist"].asJsonArray)
            } catch (t: Throwable) {
                logger.warning("Could not process 'playlist' property '${response.inputSettings["playlist"]}' for source '${source.name}'")
                t.printStackTrace()
                Notifications.add("Could not process playlist for source '${source.name}'", "OBS")
            }
        }

        source.settingsLoaded = true

        try {
            callback?.invoke()
        } catch (t: Throwable) {
            logger.severe("Failed to invoke callback after loading source settings for source '${source.name}'")
            t.printStackTrace()
            Notifications.add(
                "Something went wrong while loading scene items source settings: ${t.localizedMessage}",
                "OBS"
            )
        }

        getSourceVideoLength(source)
    }

    private fun responsePlaylistToTPlaylist(playlist: JsonArray): TPlayList {
        val videos = playlist
            .map { it.asJsonObject }
            .filter { it.has("value") }
            .map { TVideoFile(name = it["value"].asString) }

        return TPlayList(videos)
    }

    fun getSourceVideoLength(source: TSource) {
        when (source.kind) {
            "ffmpeg_source" -> getSourceLengthForMediaSource(source)
            "vlc_source" -> getSourceLengthForVLCVideoSource(source)
            else -> logger.info("Unknown kind for source '${source.name}': '${source.kind}'")
        }
    }

    fun getSourceLengthForMediaSource(source: TSource) {
        if (source.file == null) {
            logger.info("Can't load video length for ffmpeg_source if file is null for source '${source.name}")
            return
        }

        source.file!!.duration = getVideoLengthOrZeroForFile(source.file!!.name)
    }

    fun getSourceLengthForVLCVideoSource(source: TSource) {
        if (source.playlist == null) {
            logger.info("Can't load video length for vlc_source if playlist is missing for source '${source.name}")
            return
        }

        val representativeFile = TVideoFile()
        source.file = representativeFile

        source.playlist?.entries?.forEach {
            it.duration = getVideoLengthOrZeroForFile(it.name)

            if (Config.sumVlcPlaylistSourceLengths) {
                // Set first video file as source file name
                if (representativeFile.name.isEmpty()) {
                    representativeFile.name = it.name
                }

                representativeFile.duration += it.duration
            } else {
                if (it.duration <= representativeFile.duration) {
                    return@forEach
                }

                representativeFile.name = it.name
                representativeFile.duration = it.duration
            }
        }
    }

    private fun refreshGuiWithNewScenes() {
        logger.info("Refreshing GUI with new scenes")
        GUI.refreshScenes()
        OBSState.clientActivityStatus = null
        GUI.refreshOBSStatus()
    }
}