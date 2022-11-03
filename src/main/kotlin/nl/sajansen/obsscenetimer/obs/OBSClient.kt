package nl.sajansen.obsscenetimer.obs

import io.obswebsocket.community.client.OBSRemoteController
import io.obswebsocket.community.client.WebSocketCloseCode
import io.obswebsocket.community.client.listener.lifecycle.ReasonThrowable
import io.obswebsocket.community.client.message.event.scenes.CurrentProgramSceneChangedEvent
import io.obswebsocket.community.client.message.event.scenes.SceneListChangedEvent
import nl.sajansen.obsscenetimer.GUI
import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.utils.Rollbar
import org.slf4j.LoggerFactory
import java.awt.EventQueue
import java.util.*

object OBSClient {
    private var logger = LoggerFactory.getLogger(OBSClient::class.java.name)

    private var controller: OBSRemoteController? = null
    fun getController() = controller
    private var reconnecting: Boolean = false
    private var isRunning: Boolean = false
    fun isRunning() = isRunning
    private var isForceStopped: Boolean = false
    private var connectionRetryTimer: Timer? = null

    fun start() {
        if (Config.remoteSyncClientEnabled || isForceStopped) {
            isRunning = false
            return
        }
        isRunning = true

        logger.info("Connecting to OBS on: ws://${Config.obsHost}:${Config.obsPort}")
        OBSState.connectionStatus = if (!reconnecting) OBSConnectionStatus.CONNECTING else OBSConnectionStatus.RECONNECTING
        GUI.refreshOBSStatus()

        val obsPassword: String? = Config.obsPassword.ifEmpty { null }

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
        } catch (e: IllegalArgumentException) {
            if (!arrayOf("Host or Port are invalid", "Password is required").contains(e.message)) {
                Rollbar.error(e, mapOf("obsHost" to Config.obsHost, "obsPort" to Config.obsPort), "Failed to create controller")
            }
            logger.error("Failed to create controller. ${e.localizedMessage}")
            processFailedConnection("Could not connect to OBS: ${e.localizedMessage}", reconnect = false)
        } catch (e: Exception) {
            logger.error("Failed to create controller. ${e.localizedMessage}")
            Rollbar.error(e, mapOf("obsHost" to Config.obsHost, "obsPort" to Config.obsPort), "Failed to create controller")
            processFailedConnection("Could not connect to OBS: ${e.localizedMessage}", reconnect = false)
        }
    }

    fun stop(force: Boolean = false) {
        logger.info("Disconnecting with OBS (force stop=$force)")
        isForceStopped = force
        OBSState.connectionStatus = OBSConnectionStatus.CLOSING
        EventQueue.invokeLater { GUI.refreshOBSStatus() }

        connectionRetryTimer?.cancel()
        connectionRetryTimer?.purge()

        Thread {
            controller?.disconnect()
            controller?.stop()
        }.also {
            it.isDaemon = false
            it.start()
        }

        isRunning = false
    }

    private fun processFailedConnection(message: String, reconnect: Boolean = true) {
        OBSState.connectionStatus = OBSConnectionStatus.CONNECTION_FAILED
        OBSState.clientActivityStatus = null
        GUI.refreshOBSStatus()

        if (!reconnecting) {
            Notifications.add(message, "OBS")
        }

        if (reconnect) {
            startReconnectingTimeout()
        }
    }

    private fun startReconnectingTimeout() {
        if (Config.remoteSyncClientEnabled || isForceStopped) {
            reconnecting = false
            return
        }

        if (connectionRetryTimer != null) {
            return
        }

        connectionRetryTimer = Timer()
        connectionRetryTimer?.schedule(object : TimerTask() {
            override fun run() {
                reconnecting = true
                connectionRetryTimer = null
                start()
            }
        }, Config.obsReconnectionTimeout)
    }

    private fun onCurrentProgramSceneChanged(event: CurrentProgramSceneChangedEvent) {
        if (isForceStopped) return

        logger.debug("Processing scene switch event to: ${event.sceneName}")

        if (OBSState.currentScene.name == event.sceneName) {
            return
        }

        try {
            ObsSceneProcessor.processNewScene(event.sceneName)
        } catch (t: Throwable) {
            logger.error("Could not process new scene change. ${t.localizedMessage}")
            Rollbar.error(t, mapOf("event" to event), "Could not process new scene change")
            t.printStackTrace()
            Notifications.add("Could not process new scene change: ${t.localizedMessage}", "OBS")
        }
    }

    private fun onSceneListChanged(event: SceneListChangedEvent) {
        if (isForceStopped) return

        logger.debug("Processing scenes changed event for ${event.scenes.size} scenes")
        if (isForceStopped) return

        ObsSceneProcessor.loadScenes()
    }

    private fun onControllerError(throwable: ReasonThrowable) {
        logger.error("OBS Controller gave an error: ${throwable.reason}")
        throwable.throwable.printStackTrace()

        if (isForceStopped) return

        processFailedConnection("OBS connection gave an unexpected error: ${throwable.reason}", reconnect = true)
    }

    private fun onCommunicatorError(throwable: ReasonThrowable) {
        logger.error("OBS Communicator gave an error: ${throwable.reason}")
        throwable.throwable.printStackTrace()

        if (isForceStopped) return

        processFailedConnection("OBS connection gave an unexpected error: ${throwable.reason}", reconnect = true)
    }

    private fun onConnectionFailed(code: WebSocketCloseCode) {
        if (isForceStopped) {
            logger.info("Failed to connect to OBS: $code (WebSocketCloseCode)")
            return
        }
        logger.error("Failed to connect to OBS: $code (WebSocketCloseCode)")

        OBSState.connectionStatus = OBSConnectionStatus.CONNECTION_FAILED
        OBSState.clientActivityStatus = null
        Notifications.add(
            "Failed to connect to OBS: $code (WebSocketCloseCode)",
            "OBS"
        )

        GUI.refreshOBSStatus()
    }

    private fun onDisconnected() {
        logger.info("Disconnected from OBS")

        if (isForceStopped) return

        OBSState.connectionStatus = OBSConnectionStatus.DISCONNECTED
        OBSState.clientActivityStatus = null
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
            logger.error("Could not get current scene from OBS. ${t.localizedMessage}")
            Rollbar.error(t, "Could not get current scene from OBS")
            t.printStackTrace()
            Notifications.add("Could not get current scene: ${t.localizedMessage}", "OBS")
        }
    }
}
