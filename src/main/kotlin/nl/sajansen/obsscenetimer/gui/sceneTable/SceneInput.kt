package nl.sajansen.obsscenetimer.gui.sceneTable


import getTimeAsClock
import nl.sajansen.obsscenetimer.GUI
import nl.sajansen.obsscenetimer.gui.Refreshable
import nl.sajansen.obsscenetimer.objects.OBSSceneTimer
import nl.sajansen.obsscenetimer.objects.TScene
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.obs.OBSState
import nl.sajansen.obsscenetimer.themes.Theme
import org.slf4j.LoggerFactory
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
import javax.swing.BorderFactory
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


class SceneInput(val scene: TScene) : JTextField(), Refreshable {
    private val logger = LoggerFactory.getLogger(SceneInput::class.java.name)

    private val inputFont = Font(Theme.get.FONT_FAMILY, Font.PLAIN, 16)
    private val currentSceneInputFont = Font(Theme.get.FONT_FAMILY, Font.BOLD, 16)

    init {
        GUI.register(this)

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
        text = getTimeAsClock(scene.getFinalTimeLimit().toLong(), looseFormat = true)
    }

    fun getSceneTimeFromInput() {
        if (text.isEmpty()) {
            return
        }

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
                logger.warn("Failed to parse duration input for SceneInput: $this")
                t.printStackTrace()
                Notifications.add("Failed to parse duration input: '$text'. Error: ${t.localizedMessage}")
                return@forEach
            }

            val seconds = date.time / 1000L

            logger.info("Setting new scene time limit to: $seconds (parsed from: $text)")
            setNewTime(seconds.toInt())
            return
        }

        logger.warn("No input formatters found for text: '$text'")
    }

    fun setNewTime(value: Int?) {
        scene.timeLimit = value

        if (value != null && value < 0) {
            scene.resetTimeLimit()
        }

        if (scene.name == OBSState.currentScene.name) {
            OBSSceneTimer.setMaxTimerValue(scene.getFinalTimeLimit().toLong())
        }

        GUI.onSceneTimeLimitUpdated(scene)
    }

    override fun onSceneTimeLimitUpdated(scene: TScene) {
        if (scene != this.scene) return
        refreshDisplayFromScene()
    }

    override fun toString(): String {
        return "SceneInput(scene=$scene, text=$text)"
    }
}

class SceneInputKeyListener(private val input: SceneInput) : KeyListener {
    private val logger = LoggerFactory.getLogger(SceneInputKeyListener::class.java.name)

    override fun keyTyped(e: KeyEvent) {
    }

    override fun keyReleased(e: KeyEvent) {
    }

    override fun keyPressed(e: KeyEvent) {
        logger.debug("[keyPressed] in SceneInput field: ${e.keyCode}")
        when (e.keyCode) {
            KeyEvent.VK_UP -> increaseTimeLimitWith(1)
            KeyEvent.VK_DOWN -> increaseTimeLimitWith(-1)
        }
    }

    private fun increaseTimeLimitWith(amount: Int) {
        val currentLimit = input.scene.timeLimit ?: input.scene.getFinalTimeLimit()

        input.setNewTime(currentLimit + amount)
    }
}

class SceneInputDocumentListener(private val input: SceneInput) : DocumentListener {
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

class SceneInputFocusAdapter(private val input: SceneInput) : FocusAdapter() {
    private val logger = LoggerFactory.getLogger(SceneInputFocusAdapter::class.java.name)

    override fun focusLost(e: FocusEvent) {
        logger.debug("[focusLost] on SceneInput $input")
        super.focusLost(e)
        input.refreshDisplayFromScene()
    }
}