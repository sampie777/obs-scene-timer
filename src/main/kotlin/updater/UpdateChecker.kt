package updater

import com.google.gson.Gson
import com.google.gson.JsonParseException
import config.Config
import gui.updater.UpdatePopup
import objects.ApplicationInfo
import objects.notifications.Notifications
import java.awt.EventQueue
import java.net.MalformedURLException
import java.util.logging.Logger
import java.util.prefs.Preferences


class UpdateChecker(private val urlProvider: wURL = wURL()) {
    private val logger = Logger.getLogger(UpdateChecker::class.java.name)
    private val persistentSettings = Preferences.userRoot().node(UpdateChecker::class.java.name)
    private val persistentSettingsVersionReference = "latestKnownVersion"

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

        latestVersion = versions.first().trimStart('v')
        logger.fine("Latest version from remote: $latestVersion")

        if (latestVersion == ApplicationInfo.version) {
            logger.fine("Application up to date")
            return false
        }

        if (latestVersion == getLatestKnownVersion()) {
            logger.fine("Latest version hasn't changed")
            return false
        }
        updateLatestKnownVersion(latestVersion!!)

        return true
    }

    fun getRemoteTags(): List<String> {
        val jsonResponse = getRemoteTagResponse() ?: return emptyList()

        return try {
            val versionsResponse = Gson().fromJson(jsonResponse, VersionsResponseJson::class.java)
            versionsResponse.values.map(VersionsResponseVersionsJson::name)
        } catch (e: JsonParseException) {
            logger.severe("Failed to parse versions JSON response: '${jsonResponse}'")
            e.printStackTrace()
            Notifications.add(
                "Failed to check for updates: invalid JSON response. " +
                        "Please inform the developer of this application. More detailed: ${e.localizedMessage}.",
                "Updater"
            )
            emptyList()
        } catch (t: Throwable) {
            logger.severe("Failed to retrieve latest application versions: invalid response")
            t.printStackTrace()
            Notifications.add(
                "Failed to check for updates: invalid response. " +
                        "Please inform the developer of this application. More detailed: ${t.localizedMessage}.",
                "Updater"
            )
            emptyList()
        }
    }

    fun getRemoteTagResponse(): String? {
        return try {
            urlProvider.readText(ApplicationInfo.latestVersionsUrl)
        } catch (e: MalformedURLException) {
            logger.severe("Failed to retrieve latest application versions: invalid URL: '${ApplicationInfo.latestVersionsUrl}'")
            e.printStackTrace()
            Notifications.add(
                "Failed to check for updates: invalid URL. " +
                        "Please inform the developer of this application. More detailed: ${e.localizedMessage}.",
                "Updater"
            )
            null
        } catch (t: Throwable) {
            logger.severe("Failed to retrieve latest application versions")
            t.printStackTrace()
            null
        }
    }

    fun updateLatestKnownVersion(version: String) = persistentSettings.put(persistentSettingsVersionReference, version)

    fun getLatestKnownVersion(): String {
        if (persistentSettings.get(persistentSettingsVersionReference, null) == null) {
            updateLatestKnownVersion(ApplicationInfo.version)
        }
        return persistentSettings.get(persistentSettingsVersionReference, "")
    }
}