package gui.config

import config.Config
import config.PropertyLoader
import gui.config.formcomponents.*
import gui.mainFrame.WindowTitle
import objects.notifications.Notifications
import obs.ObsSceneProcessor
import themes.Theme
import java.awt.BorderLayout
import java.awt.GridLayout
import java.util.logging.Logger
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.border.EmptyBorder

class ConfigEditPanel : JPanel() {
    private val logger = Logger.getLogger(ConfigEditPanel::class.java.name)

    private val formComponents: ArrayList<FormComponent> = ArrayList()

    init {
        createFormInputs()
        createGui()
    }

    private fun createFormInputs() {
        formComponents.add(HeaderFormComponent("OBS"))
        formComponents.add(StringFormInput("obsAddress", "OBS websocket address", false))
        formComponents.add(
            StringFormInput(
                "obsPassword",
                "OBS websocket password",
                true,
                toolTipText = "This value is not stored encrypted"
            )
        )
        formComponents.add(
            NumberFormInput<Long>(
                "obsReconnectionTimeout",
                "Connection retry interval (millisec.)",
                0,
                null
            )
        )
        formComponents.add(
            BooleanFormInput(
                "autoCalculateSceneLimitsBySources",
                "Automatically calculate scene time limits by scanning video sources",
                onSave = { newValue ->
                    if (newValue == Config.autoCalculateSceneLimitsBySources) {
                        return@BooleanFormInput
                    }

                    ObsSceneProcessor.loadScenes()
                }
            )
        )
        formComponents.add(
            BooleanFormInput(
                "sumVlcPlaylistSourceLengths",
                "Sum VLC playlist source lengths instead of\nusing the longest source in the playlist",
                onSave = { newValue ->
                    if (newValue == Config.sumVlcPlaylistSourceLengths) {
                        return@BooleanFormInput
                    }

                    ObsSceneProcessor.loadScenes()
                }
            )
        )

        formComponents.add(HeaderFormComponent("GUI"))
        formComponents.add(ThemeSelectFormInput("theme", "Theme", Theme.availableThemes()))
        formComponents.add(BooleanFormInput("windowRestoreLastPosition", "Restore window position on start up"))
        formComponents.add(BooleanFormInput("mainWindowAlwaysOnTop", "Keep window always on top"))
        formComponents.add(
            StringFormInput("mainWindowTitle",
                "Window title",
                true,
                toolTipText = "You can make use of these variables: " +
                        WindowTitle.variables()
                            .joinToString(", ") { WindowTitle.VARIABLE_IDENTIFIER.format(it) }
            )
        )

        formComponents.add(HeaderFormComponent("Timer styling"))
        formComponents.add(NumberFormInput<Int>("timerCountUpFontSize", "Elapsed time timer font size (pt.)", 0, null))
        formComponents.add(
            NumberFormInput<Int>(
                "timerCountDownFontSize",
                "Remaining time timer font size (pt.)",
                0,
                null
            )
        )

        formComponents.add(HeaderFormComponent("Timer settings"))
        formComponents.add(
            NumberFormInput<Long>(
                "timerStartDelay",
                "Start timer after this delay (millisec.)",
                -1000L,
                null
            )
        )
        formComponents.add(
            NumberFormInput<Long>(
                "smallTimeDifferenceForLimitApproaching",
                "1: Show warnings if remaining time becomes less than (sec.)",
                0,
                null
            )
        )
        formComponents.add(
            NumberFormInput<Long>(
                "smallMinLimitForLimitApproaching",
                "1: Only show above warning colors for time limits greater than (sec.)",
                0,
                null
            )
        )
        formComponents.add(
            NumberFormInput<Long>(
                "largeTimeDifferenceForLimitApproaching",
                "2: Show warning if remaining time becomes less than (sec.)",
                0,
                null
            )
        )
        formComponents.add(
            NumberFormInput<Long>(
                "largeMinLimitForLimitApproaching",
                "2: Only show above warning colors for time limits greater than (sec.)",
                0,
                null
            )
        )
        formComponents.add(
            NumberFormInput<Long>(
                "timerFlashForRemainingTimeLessThan",
                "Flash screen when remaining time becomes less than (sec.)\nSet to 0 to disable flashing",
                0,
                null
            )
        )

        formComponents.add(HeaderFormComponent("Logging"))
        formComponents.add(
            BooleanFormInput(
                "enableApplicationLoggingToFile",
                "Enable application logging to a file",
                toolTipText = "Location of this logfile is shown in the Information screen, under Menu"
            )
        )
        formComponents.add(BooleanFormInput("enableSceneTimestampLogger", "Enable scene change logging"))

        formComponents.add(HeaderFormComponent("Remote Synchronisation"))
        formComponents.add(NumberFormInput<Int>("remoteSyncServerPort", "Server: Port to run on", 0, null))
        formComponents.add(StringFormInput("remoteSyncClientAddress", "Client: Remote sync server address", true))
        formComponents.add(
            NumberFormInput<Long>(
                "remoteSyncClientReconnectionTimeout",
                "Client: Connection retry interval (millisec.)",
                0,
                null
            )
        )

        formComponents.add(HeaderFormComponent("Other"))
        formComponents.add(BooleanFormInput("updatesCheckForUpdates", "Check for updates"))
    }

    private fun createGui() {
        layout = BorderLayout()

        val mainPanel = JPanel()
        mainPanel.layout = GridLayout(0, 1)
        mainPanel.border = EmptyBorder(10, 10, 10, 10)

        addConfigItems(mainPanel)

        val scrollPanelInnerPanel = JPanel(BorderLayout())
        scrollPanelInnerPanel.add(mainPanel, BorderLayout.PAGE_START)
        val scrollPanel = JScrollPane(scrollPanelInnerPanel)
        scrollPanel.border = null
        add(scrollPanel, BorderLayout.CENTER)
    }

    private fun addConfigItems(panel: JPanel) {
        formComponents.forEach {
            try {
                panel.add(it.component())
            } catch (e: Exception) {
                logger.severe("Failed to create Config Edit GUI component: ${it::class.java}")
                e.printStackTrace()

                if (it !is FormInput) {
                    return@forEach
                }

                logger.severe("Failed to create Config Edit GUI component: ${it.key}")
                Notifications.add(
                    "Failed to load GUI input for config key: <strong>${it.key}</strong>. Delete your <i>${PropertyLoader.getPropertiesFile().name}</i> file and try again. (Error: ${e.localizedMessage})",
                    "Configuration"
                )
                panel.add(TextFormComponent("Failed to load component. See Notifications.").component())
            }
        }
    }

    fun saveAll(): Boolean {
        val formInputComponents = formComponents.filterIsInstance<FormInput>()
        val validationErrors = ArrayList<String>()

        formInputComponents.forEach {
            val validation = it.validate()
            if (validation.isEmpty()) {
                return@forEach
            }

            logger.warning(validation.toString())
            validationErrors += validation
        }

        if (validationErrors.isNotEmpty()) {
            if (this.parent == null) {
                logger.warning("Panel is not a visible GUI component")
                return false
            }

            JOptionPane.showMessageDialog(
                this, validationErrors.joinToString(",\n"),
                "Invalid data",
                JOptionPane.ERROR_MESSAGE
            )
            return false
        }

        formInputComponents.forEach { it.save() }
        return true
    }
}
