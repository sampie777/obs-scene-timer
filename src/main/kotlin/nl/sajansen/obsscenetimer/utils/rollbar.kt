package nl.sajansen.obsscenetimer.utils

import com.rollbar.api.payload.data.Person
import com.rollbar.notifier.config.ConfigBuilder
import com.rollbar.notifier.provider.Provider
import nl.sajansen.obsscenetimer.ApplicationInfo
import org.slf4j.LoggerFactory
import java.util.*
import java.util.prefs.Preferences

object Rollbar : com.rollbar.notifier.Rollbar(ConfigBuilder.withAccessToken("").build()) {
    private var logger = LoggerFactory.getLogger(Rollbar::class.java.name)

    private val persistentSettings = Preferences.userRoot().node(Rollbar::class.java.name)
    private const val persistentSettingsPersonReference = "personUuid"

    private var enabled: Boolean = false

    fun enable(enable: Boolean = true) {
        if (!enable && enabled) {
            try {
                close(false)
            } catch (t: Throwable) {
                logger.error("Failed to close Rollbar instance. ${t.localizedMessage}")
                t.printStackTrace()
            }
        }

        enabled = enable
        init()
    }

    fun isEnabled() = enabled

    private fun getAccessToken(): String {
        return try {
            val properties = Properties()
            properties.load(Rollbar::class.java.getResourceAsStream("/nl/sajansen/obsscenetimer/secrets.properties"))
            val data = properties.getProperty("rollbarAccessToken")
            Base64.getDecoder().decode(data).decodeToString()
        } catch (t: Throwable) {
            logger.error("Failed to load secrets.properties. ${t.localizedMessage}")
            t.printStackTrace()
            ""
        }
    }

    private fun getEnvironment(): String {
        if (System.getProperty("env", "").isNotEmpty()) {
            return System.getProperty("env")
        }
        return if (System.getenv().containsKey("env") && System.getenv("env").isNotEmpty()) {
            System.getenv("env")
        } else "production"
    }

    fun getPersonUuid(): String {
        var uuid = persistentSettings.get(persistentSettingsPersonReference, null)
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            persistentSettings.put(persistentSettingsPersonReference, uuid)
        }
        return uuid
    }

    private fun init() {
        try {
            val config = ConfigBuilder.withAccessToken(getAccessToken())
                .environment(getEnvironment())
                .codeVersion(ApplicationInfo.version)
                .enabled(enabled)
                .platform(System.getProperty("os.name"))
                .person(PersonProvider())
                .build()
            logger.info(
                "Initializing rollbar with: " +
                        "environment=" + config.environment() + "; " +
                        "isEnabled=" + config.isEnabled + "; " +
                        "codeVersion=" + config.codeVersion() + "; "
            )
            configure(config)
        } catch (e: Exception) {
            logger.error("Failed to initialize rollbar. ${e.localizedMessage}")
            e.printStackTrace()
        }
    }
}

private class PersonProvider : Provider<Person> {
    override fun provide(): Person {
        return Person.Builder()
            .id(Rollbar.getPersonUuid())
            .build()
    }
}