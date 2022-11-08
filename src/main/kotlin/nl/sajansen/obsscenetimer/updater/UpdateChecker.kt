package nl.sajansen.obsscenetimer.updater

import com.google.gson.Gson
import com.google.gson.JsonParseException
import nl.sajansen.obsscenetimer.ApplicationInfo
import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.gui.updater.UpdatePopup
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.utils.Rollbar
import org.slf4j.LoggerFactory
import java.awt.EventQueue
import java.net.MalformedURLException
import java.net.SocketException
import java.net.UnknownHostException
import java.util.prefs.Preferences

class UpdateChecker(private val urlProvider: wURL = wURL()) {
    private val logger = LoggerFactory.getLogger(UpdateChecker::class.java.name)
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
                logger.error("Failed to check for updates. ${t.localizedMessage}")
                Rollbar.error(t, "Failed to check for updates")
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
        latestVersion = getLatestVersion()?.trimStart('v')

        if (latestVersion == null) {
            logger.info("No latest version found: $latestVersion")
            return false
        }

        logger.info("Latest version from remote: $latestVersion")

        if (latestVersion == ApplicationInfo.version) {
            logger.info("Application up to date")
            return false
        }

        if (latestVersion == getLatestKnownVersion()) {
            logger.info("Latest version hasn't changed")
            return false
        }
        updateLatestKnownVersion(latestVersion!!)

        return true
    }

    fun getLatestVersion(): String? {
        val jsonResponse = getLatestVersionResponse() ?: return null

        return try {
            Gson().fromJson(jsonResponse, LatestVersionResponseJson::class.java).tag_name
        } catch (e: JsonParseException) {
            logger.error("Failed to parse version JSON response: '${jsonResponse}'. ${e.localizedMessage}")
            Rollbar.error(e, mapOf("jsonResponse" to jsonResponse), "Failed to parse version JSON response")
            e.printStackTrace()
            Notifications.add(
                "Failed to check for updates: invalid JSON response. " +
                        "Please inform the developer of this application. More detailed: ${e.localizedMessage}.",
                "Updater"
            )
            null
        } catch (t: Throwable) {
            logger.error("Failed to retrieve latest application version: invalid response. ${t.localizedMessage}")
            Rollbar.error(t, mapOf("jsonResponse" to jsonResponse), "Failed to retrieve latest application version: invalid response")
            t.printStackTrace()
            Notifications.add(
                "Failed to check for updates: invalid response. " +
                        "Please inform the developer of this application. More detailed: ${t.localizedMessage}.",
                "Updater"
            )
            null
        }
    }

    fun getLatestVersionResponse(): String? {
        return try {
            urlProvider.readText(ApplicationInfo.latestVersionsUrl)
        } catch (e: MalformedURLException) {
            logger.error("Failed to retrieve latest application version: invalid URL: '${ApplicationInfo.latestVersionsUrl}'. ${e.localizedMessage}")
            Rollbar.error(e, mapOf("url" to ApplicationInfo.latestVersionsUrl), "Failed to retrieve latest application version: invalid URL.")
            e.printStackTrace()
            Notifications.add(
                "Failed to check for updates: invalid URL. " +
                        "Please inform the developer of this application. More detailed: ${e.localizedMessage}.",
                "Updater"
            )
            null
        } catch (e: SocketException) {
            logger.error("Failed to retrieve latest application version on url '${ApplicationInfo.latestVersionsUrl}'. ${e.localizedMessage}")
            e.printStackTrace()
            null
        } catch (e: UnknownHostException) {
            logger.error("Failed to retrieve latest application version. Internet is down?. ${e.localizedMessage}")
            null
        } catch (t: Throwable) {
            logger.error("Failed to retrieve latest application version. ${t.localizedMessage}")
            Rollbar.error(t, mapOf("url" to ApplicationInfo.latestVersionsUrl), "Failed to retrieve latest application version")
            t.printStackTrace()
            null
        }
    }

    fun updateLatestKnownVersion(version: String) = persistentSettings.put(persistentSettingsVersionReference, version)

    fun getLatestKnownVersion(): String = persistentSettings.get(persistentSettingsVersionReference, "")

    fun clearUpdateHistory() {
        logger.info("Clearing update history")
        persistentSettings.put(persistentSettingsVersionReference, "")
    }
}