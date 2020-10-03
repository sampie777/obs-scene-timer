package gui.sceneTable


import config.Config
import getTimeAsClock
import objects.OBSState
import objects.TScene
import objects.notifications.Notifications
import themes.Theme
import java.awt.Dimension
import java.awt.Font
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger
import javax.swing.BorderFactory
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


class SceneInput(val scene: TScene) : JTextField() {
    private val logger = Logger.getLogger(SceneInput::class.java.name)

    private val inputFont = Font(Theme.get.FONT_FAMILY, Font.PLAIN, 16)
    private val currentSceneInputFont = Font(Theme.get.FONT_FAMILY, Font.BOLD, 16)
    
    init {
        preferredSize = Dimension(100, 22)
        border = BorderFactory.createLineBorder(Theme.get.BORDER_COLOR)
        font = if (scene.name == OBSState.currentScene.name) currentSceneInputFont else inputFont
        horizontalAlignment = SwingConstants.RIGHT

        addKeyListener(SceneInputKeyListener(this))
        addFocusListener(SceneInputFocusAdapter(this))
        document.addDocumentListener(SceneInputDocumentListener(this))

        refreshDisplayFromScene()
    }

    fun refreshDisplayFromScene() {
        if (scene.timeLimit != null && scene.timeLimit!! < 0) {
            resetDuration()
        }

        text = getTimeAsClock(scene.getFinalTimeLimit().toLong(), looseFormat = true)
    }

    fun getSceneTimeFromInput() {
        if (text == getTimeAsClock(scene.getFinalTimeLimit().toLong(), looseFormat = true)) {
            return
        }

        val patterns = arrayOf(
            "HH:mm:ss",
            "H:mm:ss",
            "mm:ss",
            "m:ss",
            "ss",
            "s"
        )

        patterns.forEach {
            val formatter = SimpleDateFormat(it) as DateFormat
            formatter.timeZone = TimeZone.getTimeZone("UTC")

            val date = try {
                formatter.parse(text)
            } catch (e: ParseException) {
                return@forEach
            } catch (t: Throwable) {
                t.printStackTrace()
                Notifications.add("Failed to parse duration input: '$text'. Error: ${t.localizedMessage}")
                return@forEach
            }

            val seconds = date.time / 1000L
            scene.timeLimit = seconds.toInt()
            logger.info("Setting new scene time limit to: $seconds")
            return
        }

        logger.warning("Failed to parse input at all: no formatters match")
    }

    private fun resetDuration() {
        logger.info("Resetting scene's time limit")
        scene.timeLimit = null
        Config.sceneProperties.tScenes.find { it.name == scene.name }?.timeLimit = scene.maxVideoLength()
    }
}

class SceneInputKeyListener(private val input: SceneInput) : KeyListener {
    private val logger = Logger.getLogger(SceneInputKeyListener::class.java.name)

    override fun keyTyped(e: KeyEvent) {
    }

    override fun keyPressed(e: KeyEvent) {
        logger.fine("[keyPressed] in SceneInput field: ${e.keyCode}")
        when (e.keyCode) {
            KeyEvent.VK_UP -> increaseTimeLimitWith(1)
            KeyEvent.VK_DOWN -> increaseTimeLimitWith(-1)
        }
    }

    override fun keyReleased(e: KeyEvent) {
    }

    private fun increaseTimeLimitWith(amount: Int) {
        if (input.scene.timeLimit == null) {
            input.scene.timeLimit = input.scene.getFinalTimeLimit()
        }

        input.scene.timeLimit = input.scene.timeLimit?.plus(amount)

        input.refreshDisplayFromScene()
    }
}

class SceneInputDocumentListener(private val input: SceneInput): DocumentListener {
    override fun insertUpdate(e: DocumentEvent) {
        input.getSceneTimeFromInput()
    }

    override fun removeUpdate(e: DocumentEvent) {
        input.getSceneTimeFromInput()
    }

    override fun changedUpdate(e: DocumentEvent) {
        input.getSceneTimeFromInput()
    }
}

class SceneInputFocusAdapter(private val input: SceneInput): FocusAdapter() {
    override fun focusLost(e: FocusEvent) {
        super.focusLost(e)
        input.refreshDisplayFromScene()
    }
}