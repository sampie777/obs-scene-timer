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
import objects.notifications.Notification
import objects.notifications.Notifications
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class OBSClient {
    private var logger = Logger.getLogger(OBSClient::class.java.name)

    private var controller: OBSRemoteController? = null
    private val sceneListenerTimer = Timer()
    private val sceneListenerTimerInterval = 1000

    fun start() {
        logger.info("Connecting to OBS on: ${Config.obsAddress}")
        Globals.OBSConnectionStatus = OBSStatus.CONNECTING
        GUI.refreshOBSStatus()

        controller = OBSRemoteController(Config.obsAddress, false)

        if (controller!!.isFailed) { // Awaits response from OBS
            logger.severe("Failed to create controller")
            Globals.OBSConnectionStatus = OBSStatus.CONNECTION_FAILED
            GUI.refreshOBSStatus()

            Notifications.add("Could not connect to OBS", "OBS")
        }

        registerCallbacks()

        try {
            controller!!.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun registerCallbacks() {
        try {
            controller!!.registerDisconnectCallback {
                logger.info("Disconnected from OBS")
                Globals.OBSConnectionStatus = OBSStatus.DISCONNECTED
                GUI.refreshOBSStatus()

                Notifications.add(Notification("Disconnected from OBS", "OBS"))
            }
        } catch (e: Error) {
            logger.severe("Failed to create OBS callback: registerDisconnectCallback")
            e.printStackTrace()
            Notifications.add(
                "Failed to register disconnect callback: cannot notify when connection is lost",
                "OBS"
            )
        }

        try {
            controller!!.registerConnectCallback {
                logger.info("Connected to OBS")
                Globals.OBSConnectionStatus = OBSStatus.CONNECTED
                GUI.refreshOBSStatus()

                getScenes()

                getCurrentSceneFromOBS()

                startSceneWatcherTimer()
            }
        } catch (e: Error) {
            logger.severe("Failed to create OBS callback: registerConnectCallback")
            e.printStackTrace()
            Notifications.add(
                "Failed to register connect callback: scenes cannot be loaded at startup",
                "OBS"
            )
        }

        try {
            controller!!.registerScenesChangedCallback {
                logger.fine("Processing scenes changed event")
                getScenes()
            }
        } catch (e: Error) {
            logger.severe("Failed to create OBS callback: registerScenesChangedCallback")
            e.printStackTrace()
            Notifications.add(
                "Failed to register scenesChanged callback: new scenes cannot be loaded",
                "OBS"
            )
        }

        try {
            controller!!.registerSwitchScenesCallback { responseBase: ResponseBase ->
                logger.fine("Processing scene switch event")
                val response = responseBase as SwitchScenesResponse

                if (OBSSceneTimer.getCurrentSceneName() == response.sceneName) {
                    return@registerSwitchScenesCallback
                }

                processNewScene(response.sceneName)
            }
        } catch (e: Error) {
            logger.severe("Failed to create OBS callback: registerSwitchScenesCallback")
            e.printStackTrace()
            Notifications.add(
                "Failed to register switchScenes callback: cannot detect scene changes",
                "OBS"
            )
        }
    }

    private fun startSceneWatcherTimer() {
        sceneListenerTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                OBSSceneTimer.increaseTimer()
                GUI.refreshTimer()

                Config.save()
            }
        }, 0, sceneListenerTimerInterval.toLong())
    }

    /**
     * Actively request the current scene from BOS
     */
    private fun getCurrentSceneFromOBS() {
        logger.fine("Retrieving current scene")
        controller!!.getCurrentScene { res: ResponseBase ->
            val currentScene = res as GetCurrentSceneResponse

            if (OBSSceneTimer.getCurrentSceneName() == currentScene.name) {
                return@getCurrentScene
            }

            processNewScene(currentScene.name)
        }
    }

    /**
     * Set the new scene name as new current scene and notify everyone of this change
     */
    private fun processNewScene(sceneName: String) {
        OBSSceneTimer.setCurrentSceneName(sceneName)

        logger.info("New scene: " + OBSSceneTimer.getCurrentSceneName())
        OBSSceneTimer.resetTimer()

        GUI.switchedScenes()
        GUI.refreshTimer()

        SceneLogger.log(OBSSceneTimer.getCurrentSceneName())
    }

    private fun getScenes() {
        logger.info("Retrieving scenes")
        Globals.OBSActivityStatus = OBSStatus.LOADING_SCENES
        GUI.refreshOBSStatus()

        controller!!.getScenes { response: ResponseBase ->
            val res = response as GetSceneListResponse
            logger.info(res.scenes.size.toString() + " scenes retrieved")

            setOBSScenes(res.scenes)
        }
    }

    private fun setOBSScenes(scenes: List<Scene>) {
        Globals.scenes.clear()
        for (scene in scenes) {
            val tScene = TScene()
            tScene.name = scene.name

            if (scene.sources != null) {
                val tSources = scene.sources.map { source: Source -> TSource(source.name, source.type) }

                tScene.sources = tSources
            }

            Globals.scenes[tScene.name] = tScene
        }

        if (!loadSourceSettings()) {
            GUI.refreshScenes()
            Globals.OBSActivityStatus = null
            GUI.refreshOBSStatus()
        }
    }

    /**
     * Loads the scene sources. If there are no sources, or if it can't load the sources, it will return false.
     * Otherwise it will return true and will eventually call GUI.INSTANCE.refreshScenes();
     * @return boolean
     */
    private fun loadSourceSettings(): Boolean {
        if (!isAddressLocalhost(Config.obsAddress)) {
            logger.info("Not going to try to get the video lengths, because the source files are probably running on another computer")
            return false
        }

        Globals.OBSActivityStatus = OBSStatus.LOADING_SCENE_SOURCES
        GUI.refreshOBSStatus()

        val sources: List<TSource> = Globals.scenes.values
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
                    Globals.OBSActivityStatus = null
                    GUI.refreshOBSStatus()
                } else {
                    loadSourceSettings(sources, sourceNames)
                }
            }
        } catch (e: Error) {
            logger.severe("Failed to load source settings for source: $sourceName")
            e.printStackTrace()
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
                videoLength = getVideoLength(source.fileName).toInt()
            } catch (e: Exception) {
                logger.severe("Failed to get video length: $e")
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
    private fun getVideoLength(filename: String): Long {
        val file = File(filename)
        if (!file.exists()) {
            logger.warning("File does not exists: $filename")
            return 0
        }

        logger.info("Getting duration of: $filename")

        val container = IContainer.make()
        container.open(filename, IContainer.Type.READ, null)
        val duration = container.duration

        return TimeUnit.MICROSECONDS.toSeconds(duration)
    }
}
