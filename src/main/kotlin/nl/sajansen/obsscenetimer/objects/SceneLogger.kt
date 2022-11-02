package nl.sajansen.obsscenetimer.objects

import getCurrentJarDirectory
import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*


object SceneLogger {
    private val logger = LoggerFactory.getLogger(SceneLogger.toString())

    private val file: File?

    init {
        if (!Config.enableSceneTimestampLogger) {
            file = null
        } else {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date())
            file = File(getCurrentJarDirectory(this).absolutePath + File.separatorChar + "sceneLogger_${timestamp}.csv")

            checkAndCreateFile()
        }
    }

    fun log(sceneName: String) {
        if (!Config.enableSceneTimestampLogger) {
            return
        }

        if (file == null) {
            logger.error("Cannot log scenes because sceneLogger file is null")
            return
        }

        val timestamp = System.currentTimeMillis()
        val dataLine = "${timestamp};\"${sceneName}\"\n"

        logger.debug("Appending scene to sceneLogger: $dataLine")
        try {
            val fileWriter = FileWriter(file, true)
            fileWriter.append(dataLine)
            fileWriter.flush()
            fileWriter.close()
        } catch (e: Exception) {
            logger.error("Failed to write to sceneLogger file. ${e.localizedMessage}")
            e.printStackTrace()

            Notifications.add("Failed to write to sceneLogger file", "SceneLogger")
        }
    }

    private fun checkAndCreateFile() {
        if (file == null) {
            logger.error("Cannot create sceneLogger file because no file is given")
            return
        }

        if (file.exists()) {
            return
        }

        logger.info("Creating sceneLogger file: ${file.absolutePath}")
        try {
            file.createNewFile()
        } catch (e: Exception) {
            logger.error("Failed to create sceneLogger file. ${e.localizedMessage}")
            e.printStackTrace()

            Notifications.add("Failed to create sceneLogger file", "SceneLogger")
        }
    }
}