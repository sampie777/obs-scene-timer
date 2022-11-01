package nl.sajansen.obsscenetimer.objects

import getCurrentJarDirectory
import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger


object SceneLogger {
    private val logger = Logger.getLogger(SceneLogger.toString())

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
            logger.severe("Cannot log scenes because sceneLogger file is null")
            return
        }

        val timestamp = System.currentTimeMillis()
        val dataLine = "${timestamp};\"${sceneName}\"\n"

        logger.fine("Appending scene to sceneLogger: $dataLine")
        try {
            val fileWriter = FileWriter(file, true)
            fileWriter.append(dataLine)
            fileWriter.flush()
            fileWriter.close()
        } catch (e: Exception) {
            logger.severe("Failed to write to sceneLogger file")
            e.printStackTrace()

            Notifications.add("Failed to write to sceneLogger file", "SceneLogger")
        }
    }

    private fun checkAndCreateFile() {
        if (file == null) {
            logger.severe("Cannot create sceneLogger file because no file is given")
            return
        }

        if (file.exists()) {
            return
        }

        logger.info("Creating sceneLogger file: ${file.absolutePath}")
        try {
            file.createNewFile()
        } catch (e: Exception) {
            logger.severe("Failed to create sceneLogger file")
            e.printStackTrace()

            Notifications.add("Failed to create sceneLogger file", "SceneLogger")
        }
    }
}