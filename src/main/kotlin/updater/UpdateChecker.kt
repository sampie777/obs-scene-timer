package updater

import com.google.gson.Gson
import config.Config
import gui.updater.UpdatePopup
import objects.ApplicationInfo
import objects.notifications.Notifications
import java.awt.EventQueue
import java.net.MalformedURLException
import java.util.logging.Logger


class UpdateChecker(private val urlProvider: wURL = wURL()) {
    private val logger = Logger.getLogger(UpdateChecker::class.java.name)

    private var latestVersion: String? = null

    fun checkForUpdates() {
        if (!Config.updatesCheckForUpdates) {
            return
        }

        Thread {
            try {
                checkForUpdatesThread()
            } catch (t: Throwable) {
                logger.severe("Failed to check for updates")
                t.printStackTrace()
                Notifications.add(
                    "Failed to check for updates. More detailed: ${t.localizedMessage}",
                    "Updater"
                )
            }
        }.start()
    }

    private fun checkForUpdatesThread() {
        logger.info("Checking for updates...")
        if (!isNewUpdateAvailable() || latestVersion == null) {
            logger.info("No new updates available")
            return
        }

        logger.info("New update found: $latestVersion")
        showNewUpdatePopup()
    }

    private fun showNewUpdatePopup() {
        EventQueue.invokeLater {
            UpdatePopup.createAndShow(latestVersion!!)
        }
    }

    fun isNewUpdateAvailable(): Boolean {
        val versions = getRemoteTags()
        logger.fine("Retrieved ${versions.size} versions from remote: $versions")

        if (versions.isEmpty()) {
            return false
        }

        latestVersion = versions.first()
        logger.fine("Latest version from remote: $latestVersion")
        return latestVersion != ApplicationInfo.version
    }

    fun getRemoteTags(): List<String> {
        val jsonResponse = getRemoteTagResponse()
        val versionsResponse = Gson().fromJson(jsonResponse, VersionsResponseJson::class.java)
        return versionsResponse.values.map(VersionsResponseVersionsJson::name)
    }

    private fun getRemoteTagResponse(): String {
        return urlProvider.readText(latestVersionsUrl)
    }
}