package obs

import GUI
import config.Config
import io.obswebsocket.community.client.OBSRemoteController
import io.obswebsocket.community.client.WebSocketCloseCode
import io.obswebsocket.community.client.listener.lifecycle.ReasonThrowable
import io.obswebsocket.community.client.message.event.scenes.CurrentProgramSceneChangedEvent
import io.obswebsocket.community.client.message.event.scenes.SceneListChangedEvent
import objects.notifications.Notifications
import java.util.*
import java.util.logging.Logger

object OBSClient {
    private var logger = Logger.getLogger(OBSClient::class.java.name)

    private var controller: OBSRemoteController? = null
    fun getController() = controller
    private var reconnecting: Boolean = false
    private var isRunning: Boolean = false
    fun isRunning() = isRunning

    fun start() {
        if (Config.remoteSyncClientEnabled) {
            isRunning = false
            return
        }
        isRunning = true

        logger.info("Connecting to OBS on: ${Config.obsAddress}")
        OBSState.connectionStatus = if (!reconnecting) OBSConnectionStatus.CONNECTING else OBSConnectionStatus.RECONNECTING
        GUI.refreshOBSStatus()

        val obsPassword: String? = if (Config.obsPassword.isEmpty()) null else Config.obsPassword

        val builder = OBSRemoteController.builder()
            .host(Config.obsHost)
            .port(Config.obsPort)
            .password(obsPassword)
            .connectionTimeout(3)
            .lifecycle()
            .onReady(OBSClient::onConnected)
            .onDisconnect(OBSClient::onDisconnected)
            .onClose(OBSClient::onConnectionFailed)
            .onControllerError(OBSClient::onControllerError)
            .onCommunicatorError(OBSClient::onCommunicatorError)
            .and()
            .registerEventListener(SceneListChangedEvent::class.java, OBSClient::onSceneListChanged)
            .registerEventListener(CurrentProgramSceneChangedEvent::class.java, OBSClient::onCurrentProgramSceneChanged)
            .autoConnect(true)

        try {
            controller = builder.build()
        } catch (e: Exception) {
            logger.severe("Failed to create controller")
            processFailedConnection("Could not connect to OBS: ${e.localizedMessage}", reconnect = false)
        }
    }

    fun stop() {
        logger.info("Disconnecting with OBS")
        controller?.disconnect()

        isRunning = false
    }

    private fun processFailedConnection(message: String, reconnect: Boolean = true) {
        OBSState.connectionStatus = OBSConnectionStatus.CONNECTION_FAILED
        GUI.refreshOBSStatus()

        if (!reconnecting) {
            Notifications.add(message, "OBS")
        }

        if (reconnect) {
            startReconnectingTimeout()
        }
    }

    private fun startReconnectingTimeout() {
        if (Config.remoteSyncClientEnabled) {
            reconnecting = false
            return
        }

        val connectionRetryTimer = Timer()
        connectionRetryTimer.schedule(object : TimerTask() {
            override fun run() {
                reconnecting = true
                start()
            }
        }, Config.obsReconnectionTimeout)
    }

    private fun onCurrentProgramSceneChanged(event: CurrentProgramSceneChangedEvent) {
        logger.fine("Processing scene switch event to: ${event.sceneName}")

        if (OBSState.currentScene.name == event.sceneName) {
            return
        }

        try {
            ObsSceneProcessor.processNewScene(event.sceneName)
        } catch (t: Throwable) {
            logger.severe("Could not process new scene change")
            t.printStackTrace()
            Notifications.add("Could not process new scene change: ${t.localizedMessage}", "OBS")
        }
    }

    private fun onSceneListChanged(event: SceneListChangedEvent) {
        logger.fine("Processing scenes changed event for ${event.scenes.size} scenes")
        ObsSceneProcessor.loadScenes()
    }

    private fun onControllerError(throwable: ReasonThrowable) {
        logger.severe("OBS Controller gave an error: ${throwable.reason}")
        throwable.throwable.printStackTrace()

        Notifications.add("OBS Connection module gave an unexpected error: ${throwable.reason}", "OBS")
    }

    private fun onCommunicatorError(throwable: ReasonThrowable) {
        logger.severe("OBS Communicator gave an error: ${throwable.reason}")
        throwable.throwable.printStackTrace()

        Notifications.add("OBS Connection module gave an unexpected error: ${throwable.reason}", "OBS")
    }

    private fun onConnectionFailed(code: WebSocketCloseCode) {
        logger.severe("Failed to connect to OBS: $code (WebSocketCloseCode)")
        OBSState.connectionStatus = OBSConnectionStatus.CONNECTION_FAILED
        Notifications.add(
            "Failed to connect to OBS: $code (WebSocketCloseCode)",
            "OBS"
        )

        GUI.refreshOBSStatus()
    }

    private fun onDisconnected() {
        logger.info("Disconnected from OBS")
        OBSState.connectionStatus = OBSConnectionStatus.DISCONNECTED
        GUI.refreshOBSStatus()

        Notifications.add("Disconnected from OBS", "OBS")

        startReconnectingTimeout()
    }

    private fun onConnected() {
        logger.info("Connected to OBS")
        OBSState.connectionStatus = OBSConnectionStatus.CONNECTED
        GUI.refreshOBSStatus()

        if (reconnecting) {
            Notifications.add("Connection re-established", "OBS", markAsRead = true)
        }
        reconnecting = false

        ObsSceneProcessor.loadScenes()

        try {
            ObsSceneProcessor.getCurrentSceneFromOBS()
        } catch (t: Throwable) {
            logger.severe("Could not get current scene from OBS")
            t.printStackTrace()
            Notifications.add("Could not get current scene: ${t.localizedMessage}", "OBS")
        }
    }
}
