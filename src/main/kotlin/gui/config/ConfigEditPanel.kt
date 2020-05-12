package gui.config

import gui.config.formcomponents.*
import objects.notifications.Notifications
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
        formComponents.add(StringFormInput("obsPassword", "OBS websocket password", true, toolTipText = "This value is not stored encrypted"))
        formComponents.add(
            NumberFormInput<Long>(
                "obsReconnectionTimeout",
                "Connection retry interval (millisec.)",
                0,
                null
            )
        )
        formComponents.add(HeaderFormComponent("Theme"))
        formComponents.add(ThemeSelectFormInput("theme", "Theme", Theme.availableThemes()))

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

        formComponents.add(HeaderFormComponent("Scene logger"))
        formComponents.add(BooleanFormInput("enableSceneTimestampLogger", "Enable scene change logging"))
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
                    "Failed to load GUI input for config key: <strong>${it.key}</strong>. Delete your <i>user.properties</i> file and try again.",
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
