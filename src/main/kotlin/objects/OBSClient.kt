package objects

import GUI
import com.xuggle.xuggler.IContainer
import config.Config
import isAddressLocalhost
import net.twasi.obsremotejava.OBSRemoteController
import net.twasi.obsremotejava.events.responses.SwitchScenesResponse
import net.twasi.obsremotejava.objects.Scene
import net.twasi.obsremotejava.objects.Source
import net.twasi.obsremotejava.requests.GetCurrentScene.GetCurrentSceneResponse
import net.twasi.obsremotejava.requests.GetSceneList.GetSceneListResponse
import net.twasi.obsremotejava.requests.GetSourceSettings.GetSourceSettingsResponse
import net.twasi.obsremotejava.requests.ResponseBase
import objects.notifications.Notifications
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

object OBSClient {
    private var logger = Logger.getLogger(OBSClient::class.java.name)

    private var controller: OBSRemoteController? = null
    private var reconnecting: Boolean = false

    fun start() {
        logger.info("Connecting to OBS on: ${Config.obsAddress}")
        OBSState.connectionStatus = if (!reconnecting) OBSClientStatus.CONNECTING else OBSClientStatus.RECONNECTING
        GUI.refreshOBSStatus()

        val obsPassword: String? = if (Config.obsPassword.isEmpty()) null else Config.obsPassword

        controller = OBSRemoteController(Config.obsAddress, false, obsPassword)

        // Await response from OBS
        if (controller!!.isFailed) {
            logger.severe("Failed to create controller")
            processFailedConnection("Could not connect to OBS", reconnect = true)
        }

        registerCallbacks()

        try {
            controller!!.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
            Notifications.add("Connection with OBS suddenly stopped", "OBS")
        }
    }

    private fun processFailedConnection(message: String, reconnect: Boolean = true) {
        OBSState.connectionStatus = OBSClientStatus.CONNECTION_FAILED
        GUI.refreshOBSStatus()

        if (!reconnecting) {
            Notifications.add(message, "OBS")
        }

        if (reconnect) {
            startReconnectingTimeout()
        }
    }

    private fun startReconnectingTimeout() {
        val connectionRetryTimer = Timer()
        connectionRetryTimer.schedule(object : TimerTask() {
            override fun run() {
                reconnecting = true
                start()
            }
        }, Config.obsReconnectionTimeout)
    }

    private fun registerCallbacks() {
        try {
            controller!!.registerOnError { message, throwable ->
                logger.severe("OBS Controller gave an error: $message")
                throwable.printStackTrace()

                Notifications.add("OBS Connection module gave an unexpected error: $message", "OBS")
            }
        } catch (t: Throwable) {
            logger.severe("Failed to create OBS callback: registerOnError")
            t.printStackTrace()
            Notifications.add(
                "Failed to register error callback: cannot notify when unexpected errors occur",
                "OBS"
            )
        }

        try {
            controller!!.registerDisconnectCallback {
                logger.info("Disconnected from OBS")
                OBSState.connectionStatus = OBSClientStatus.DISCONNECTED
                GUI.refreshOBSStatus()

                Notifications.add("Disconnected from OBS", "OBS")

                startReconnectingTimeout()
            }
        } catch (t: Throwable) {
            logger.severe("Failed to create OBS callback: registerDisconnectCallback")
            t.printStackTrace()
            Notifications.add(
                "Failed to register disconnect callback: cannot notify when connection is lost",
                "OBS"
            )
        }

        try {
            controller!!.registerConnectCallback {
                logger.info("Connected to OBS")
                OBSState.connectionStatus = OBSClientStatus.CONNECTED
                GUI.refreshOBSStatus()

                if (reconnecting) {
                    Notifications.add("Connection re-established", "OBS")
                }
                reconnecting = false

                getScenes()

                getCurrentSceneFromOBS()
            }
        } catch (t: Throwable) {
            logger.severe("Failed to create OBS callback: registerConnectCallback")
            t.printStackTrace()
            Notifications.add(
                "Failed to register connect callback: scenes cannot be loaded at startup",
                "OBS"
            )
        }

        try {
            controller!!.registerConnectionFailedCallback { message: String ->
                logger.severe("Failed to connect to OBS: $message")
                OBSState.connectionStatus = OBSClientStatus.CONNECTION_FAILED
                Notifications.add(
                    "Failed to connect to OBS: $message",
                    "OBS"
                )

                GUI.refreshOBSStatus()
            }
        } catch (t: Throwable) {
            logger.severe("Failed to create OBS callback: registerConnectionFailedCallback")
            t.printStackTrace()
            Notifications.add(
                "Failed to register connectionFailed callback: connection failures won't be shown",
                "OBS"
            )
        }

        try {
            controller!!.registerScenesChangedCallback {
                logger.fine("Processing scenes changed event")
                getScenes()
            }
        } catch (t: Throwable) {
            logger.severe("Failed to create OBS callback: registerScenesChangedCallback")
            t.printStackTrace()
            Notifications.add(
                "Failed to register scenesChanged callback: new scenes cannot be loaded",
                "OBS"
            )
        }

        try {
            controller!!.registerSwitchScenesCallback { responseBase: ResponseBase ->
                logger.fine("Processing scene switch event")
                val response = responseBase as SwitchScenesResponse

                if (OBSState.currentSceneName == response.sceneName) {
                    return@registerSwitchScenesCallback
                }

                processNewScene(response.sceneName)
            }
        } catch (t: Throwable) {
            logger.severe("Failed to create OBS callback: registerSwitchScenesCallback")
            t.printStackTrace()
            Notifications.add(
                "Failed to register switchScenes callback: cannot detect scene changes",
                "OBS"
            )
        }
    }

    /**
     * Actively request the current scene from BOS
     */
    private fun getCurrentSceneFromOBS() {
        logger.fine("Retrieving current scene")
        controller!!.getCurrentScene { res: ResponseBase ->
            val currentScene = res as GetCurrentSceneResponse

            if (OBSState.currentSceneName == currentScene.name) {
                return@getCurrentScene
            }

            processNewScene(currentScene.name)
        }
    }

    /**
     * Set the new scene name as new current scene and notify everyone of this change
     */
    fun processNewScene(sceneName: String) {
        logger.info("New scene: $sceneName")
        OBSState.currentSceneName = sceneName

        OBSSceneTimer.reset()

        GUI.switchedScenes()
        GUI.refreshTimer()

        SceneLogger.log(OBSState.currentSceneName)
    }

    private fun getScenes() {
        logger.info("Retrieving scenes")
        OBSState.clientActivityStatus = OBSClientStatus.LOADING_SCENES
        GUI.refreshOBSStatus()

        try {
            controller!!.getScenes { response: ResponseBase ->
                val res = response as GetSceneListResponse
                logger.info(res.scenes.size.toString() + " scenes retrieved")

                try {
                    processOBSScenesToOBSStateScenes(res.scenes)
                } catch (e: Exception) {
                    logger.severe("Failed to process scenes")
                    e.printStackTrace()
                    Notifications.add("Something went wrong during scenes processing", "OBS")
                }
            }
        } catch (e: Exception) {
            logger.severe("Failed to retrieve scenes")
            e.printStackTrace()
            Notifications.add("Something went wrong during retrieving scenes", "OBS")
        }
    }

    fun processOBSScenesToOBSStateScenes(scenes: List<Scene>) {
        logger.info("Set the OBS Scenes")
        OBSState.scenes.clear()
        for (scene in scenes) {
            val tScene = TScene(scene.name)

            if (scene.sources != null && Config.autoCalculateSceneLimitsBySources) {
                val tSources = scene.sources.map { source: Source -> TSource(source.name, source.type) }

                tScene.sources = tSources
            }

            OBSState.scenes.add(tScene)
        }

        val sourceSettingsAreLoading = try {
            loadSourceSettings()
        } catch (e: Exception) {
            logger.severe("Failed to load scene sources settings")
            e.printStackTrace()
            Notifications.add("Something went wrong while processing scene sources settings", "OBS")
            false
        }

        if (!sourceSettingsAreLoading) {
            logger.info("Refreshing scenes info")
            GUI.refreshScenes()
            OBSState.clientActivityStatus = null
            GUI.refreshOBSStatus()
        }
    }

    /**
     * Loads the scene sources. If there are no sources, or if it can't load the sources, it will return false.
     * Otherwise it will return true and will eventually call GUI.INSTANCE.refreshScenes();
     * @return boolean
     */
    fun loadSourceSettings(): Boolean {
        if (Config.autoCalculateSceneLimitsBySources) {
            logger.info("Auto calculation of scene time limits by source files is disabled")
            return false
        }

        if (!isAddressLocalhost(Config.obsAddress)) {
            logger.info("Not going to try to get the video lengths, because the source files are probably running on another computer")
            return false
        }

        OBSState.clientActivityStatus = OBSClientStatus.LOADING_SCENE_SOURCES
        GUI.refreshOBSStatus()

        val sources: List<TSource> = OBSState.scenes
            .flatMap { tScene: TScene -> tScene.sources }

        val sourceNames: MutableList<String> = sources
            .map { it.name }
            .distinct()
            .toMutableList()

        if (sources.isNotEmpty()) {
            loadSourceSettings(sources, sourceNames)
            return true
        }
        return false
    }

    private fun loadSourceSettings(sources: List<TSource>, sourceNames: MutableList<String>) {
        val sourceName: String = sourceNames.removeAt(0)
        logger.info("Loading source settings for source: $sourceName")

        try {
            controller!!.getSourceSettings(sourceName) { response: ResponseBase ->
                val res = response as GetSourceSettingsResponse
                logger.info("Processing received source settings for source: ${res.sourceName}")

                // Apply response to all matching sources
                sources.filter { it.name == res.sourceName }
                    .forEach { assignSourceSettingsFromOBSResponse(it, res) }

                if (sourceNames.isEmpty()) {
                    GUI.refreshScenes()
                    OBSState.clientActivityStatus = null
                    GUI.refreshOBSStatus()
                } else {
                    loadSourceSettings(sources, sourceNames)
                }
            }
        } catch (t: Throwable) {
            logger.severe("Failed to load source settings for source: $sourceName")
            t.printStackTrace()
            Notifications.add(
                "Failed to load sources information",
                "OBS"
            )
        }
    }

    private fun assignSourceSettingsFromOBSResponse(source: TSource, response: GetSourceSettingsResponse) {
        source.settings = response.sourceSettings
        source.type = response.sourceType

        if ("ffmpeg_source" == source.type && source.settings.containsKey("local_file")) {
            source.fileName = source.settings["local_file"] as String

            var videoLength = 0
            try {
                logger.info("Trying to get video length for: ${source.fileName}")
                videoLength = getVideoLength(source.fileName).toInt()
            } catch (t: Throwable) {
                t.printStackTrace()
                logger.severe("Failed to get video length: $t")
            }
            source.videoLength = videoLength
        }
    }

    /**
     * Returns video length in seconds, or 0 if file not found
     *
     * @param filename
     * @return
     */
    fun getVideoLength(filename: String): Long {
        val file = File(filename)
        if (!file.exists()) {
            logger.warning("File does not exists: $filename")
            return 0
        }

        logger.info("Getting duration of: $filename")

        val container = IContainer.make()
        container.open(filename, IContainer.Type.READ, null)
        val duration = TimeUnit.MICROSECONDS.toSeconds(container.duration)
        logger.info("Duration is: $duration")

        return duration
    }
}
