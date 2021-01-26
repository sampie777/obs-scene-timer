package updater

import com.google.gson.Gson
import objects.ApplicationInfo


class UpdateChecker(private val urlProvider: wURL = wURL()) {
    private val latestVersionsUrl = "https://api.bitbucket.org/2.0/repositories/sajansen/obs-scene-timer/refs/tags\\?sort\\=-name"

    fun isNewUpdateAvailable(): Boolean {
        val versions = getRemoteTags()
        val newestVersion = versions.first()

        return newestVersion != ApplicationInfo.version
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