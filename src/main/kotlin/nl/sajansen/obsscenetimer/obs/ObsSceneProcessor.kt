package nl.sajansen.obsscenetimer.obs

import com.google.gson.JsonArray
import invokeWithCatch
import io.obswebsocket.community.client.message.response.inputs.GetInputSettingsResponse
import io.obswebsocket.community.client.message.response.sceneitems.GetSceneItemListResponse
import io.obswebsocket.community.client.message.response.scenes.GetCurrentProgramSceneResponse
import io.obswebsocket.community.client.message.response.scenes.GetSceneListResponse
import io.obswebsocket.community.client.model.Scene
import io.obswebsocket.community.client.model.SceneItem
import isAddressLocalhost
import nl.sajansen.obsscenetimer.GUI
import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.objects.*
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.utils.getVideoLengthOrZeroForFile
import java.net.ConnectException
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

        GUI.refreshScenes()

        loadSceneItems(callback = {
            OBSState.clientActivityStatus = null
            GUI.refreshOBSStatus()
        })
    }

    private fun loadSceneItems(callback: (() -> Unit)? = null) {
        if (!isAddressLocalhost(Config.obsAddress)) {
            logger.info("Not going to try to get the video lengths, because the source files are probably running on another computer")
            callback?.invokeWithCatch(logger, { "Failed to invoke callback when skipping loadSceneItems" })
            return
        }

        if (OBSState.scenes.isEmpty()) {
            callback?.invokeWithCatch(
                logger,
                { "Failed to invoke callback during loadSceneItems" },
                { "Something went wrong after loading scene items: ${it.localizedMessage}" }
            )
            return
        }

        OBSState.scenes.forEach { scene ->
            try {
                loadItemsForScene(scene) { loadSourceSettingsForScene(scene = scene, callback = callback) }
            } catch (t: Throwable) {
                logger.severe("Failed to load scene items for scene '${scene.name}'")
                t.printStackTrace()
                Notifications.add(
                    "Could not load scene items for scene '${scene.name}': ${t.localizedMessage}",
                    "OBS"
                )

                callback?.invokeWithCatch(logger, { "Failed to invoke callback after loadItemsForScene failed" })
            }
        }
    }

    private fun loadItemsForScene(scene: TScene, callback: (() -> Unit)? = null) {
        if (OBSState.connectionStatus != OBSConnectionStatus.CONNECTED) {
            throw ConnectException("OBS not connected")
        }

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

            if (scene.sources.isEmpty()) {
                GUI.onSceneTimeLimitUpdated(scene)
            }

            callback?.invokeWithCatch(
                logger,
                { "Failed to invoke callback after loading scene items for scene '${scene.name}'" },
                { "Something went wrong while loading scene items: ${it.localizedMessage}" }
            )
        }
    }

    private fun addSceneItemToTScene(scene: TScene, item: SceneItem) {
        logger.info("Adding item '${item.sourceName}' to scene: '${scene.name}'")
        scene.sources.add(TSource(name = item.sourceName, kind = item.inputKind))
    }

    fun loadSourceSettingsForScene(scene: TScene, forceAutoCalculation: Boolean = false, callback: (() -> Unit)? = null): Boolean {
        if (!forceAutoCalculation && !Config.autoCalculateSceneLimitsBySources) {
            logger.info("Auto calculation of scene time limits by source files is disabled")
            callback?.invokeWithCatch(logger, { "Failed to invoke callback when skipping loadSourceSettingsForScene" })
            return false
        }

        if (!isAddressLocalhost(Config.obsAddress)) {
            logger.info("Not going to try to get the video lengths, because the source files are probably running on another computer")
            callback?.invokeWithCatch(logger, { "Failed to invoke callback when skipping loadSourceSettingsForAllScenes" })
            return false
        }

        OBSState.clientActivityStatus = OBSClientStatus.LOADING_SCENE_SOURCES
        GUI.refreshOBSStatus()

        val sources = scene.sources.filter { !it.settingsLoaded }

        if (sources.isEmpty()) {
            callback?.invokeWithCatch(
                logger,
                { "Failed to invoke callback during loadSourceSettingsForScene" },
                { "Something went wrong after loading scene item sources: ${it.localizedMessage}" })
            return true
        }

        sources.forEach { source ->
            try {
                loadSourceSettingsForSource(source, callback = callback)
            } catch (t: Throwable) {
                logger.severe("Failed to load scene item sources settings for item '${source.name}'")
                t.printStackTrace()
                Notifications.add(
                    "Could not process scene item sources settings for item '${source.name}': ${t.localizedMessage}",
                    "OBS"
                )

                callback?.invokeWithCatch(logger, { "Failed to invoke callback after loadSourceSettingsForSource failed" })
            }
        }
        return true
    }

    private fun loadSourceSettingsForSource(source: TSource, callback: (() -> Unit)? = null) {
        // First check our scenes for matching sources
        val existingSource = OBSState.scenes.flatMap { it.sources }
            .find { it.settingsLoaded && it != source && it.name == source.name }

        if (existingSource != null) {
            logger.info("Copying existing settings from matching source '${source.name}'.")
            existingSource.copyTo(source)
            source.settingsLoaded = true

            OBSState.scenes.filter { it.sources.contains(source) }
                .forEach(GUI::onSceneTimeLimitUpdated)

            callback?.invokeWithCatch(
                logger,
                { "Failed to invoke callback after copying source settings for source '${source.name}'" },
                { "Something went wrong while loading scene items source settings: ${it.localizedMessage}" })
            return
        }

        if (OBSState.connectionStatus != OBSConnectionStatus.CONNECTED) {
            throw ConnectException("OBS not connected")
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

        callback?.invokeWithCatch(
            logger,
            { "Failed to invoke callback after loading source settings for source '${source.name}'" },
            { "Something went wrong while loading scene items source settings: ${it.localizedMessage}" })

        getSourceVideoLength(source)

        source.settingsLoaded = true

        OBSState.scenes.filter { it.sources.contains(source) }
            .forEach(GUI::onSceneTimeLimitUpdated)
    }

    private fun responsePlaylistToTPlaylist(playlist: JsonArray): TPlayList {
        val videos = playlist
            .map { it.asJsonObject }
            .filter { it.has("value") }
            .map { TVideoFile(name = it["value"].asString) }

        return TPlayList(videos)
    }

    private fun getSourceVideoLength(source: TSource) {
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
        OBSState.clientActivityStatus = null
        GUI.refreshScenes()
        GUI.refreshOBSStatus()
    }
}