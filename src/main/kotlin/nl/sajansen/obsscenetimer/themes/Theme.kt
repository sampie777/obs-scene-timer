package nl.sajansen.obsscenetimer.themes

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.utils.Rollbar
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Insets
import javax.swing.UIManager


object Theme {
    private val logger = LoggerFactory.getLogger(Theme::class.java.name)

    lateinit var get: BaseTheme
    private val themeList: ArrayList<ThemeWrapper> = arrayListOf(
        ThemeWrapper("LightTheme", "Light", LightTheme::class.java),
        ThemeWrapper("DarkTheme", "Dark", DarkTheme::class.java)
    )

    init {
        set(Config.theme)
    }

    fun init() {
        set(Config.theme)
        apply()
    }

    private fun set(themeInternalName: String) {
        try {
            val newTheme = loadTheme(themeInternalName)
            if (newTheme == null) {
                logger.warn("Could not find theme '$themeInternalName'. Using default theme.")

                if (themeList.size == 0) {
                    throw IndexOutOfBoundsException("No themes available")
                }

                get = loadTheme(themeList[0].internalName)!!
                return
            }

            get = newTheme
        } catch (e: Exception) {
            logger.error("Failed to set theme to '$themeInternalName'. ${e.localizedMessage}")
            Rollbar.error(e, "Failed to set theme to '$themeInternalName'")
            e.printStackTrace()
            Notifications.add("Failed to set theme to '$themeInternalName': ${e.localizedMessage}", "Theme")
        }
    }

    private fun loadTheme(themeInternalName: String): BaseTheme? {
        val themeWrapper = themeList.find { themeInternalName == it.internalName } ?: return null
        return themeWrapper.clazz.newInstance() as BaseTheme
    }

    fun availableThemes(): List<ThemeWrapper> {
        return themeList
    }

    fun addTheme(themeInternalName: String, themeDisplayName: String, themeClass: Class<*>) {
        themeList.add(ThemeWrapper(themeInternalName, themeDisplayName, themeClass))
    }

    private fun apply() {
        UIManager.put("Panel.background", get.BACKGROUND_COLOR)
        UIManager.put("Panel.foreground", get.FONT_COLOR)
        UIManager.put("SplitPaneDivider.draggingColor", get.BACKGROUND_COLOR)
        UIManager.put("SplitPane.background", get.BACKGROUND_COLOR)
        UIManager.put("Label.background", get.BACKGROUND_COLOR)
        UIManager.put("Label.foreground", get.FONT_COLOR)
        UIManager.put("List.background", get.LIST_BACKGROUND_COLOR)
        UIManager.put("List.foreground", get.FONT_COLOR)
        UIManager.put("List.selectionBackground", get.LIST_SELECTION_BACKGROUND_COLOR)
        UIManager.put("List.selectionForeground", get.LIST_SELECTION_FONT_COLOR_DEFAULT)
        UIManager.put("MenuBar.background", get.BACKGROUND_COLOR)
        UIManager.put("MenuBar.foreground", get.FONT_COLOR)
        UIManager.put("MenuBar.borderColor", get.MENU_BAR_BORDER_COLOR)
        UIManager.put("Menu.background", get.BACKGROUND_COLOR)
        UIManager.put("Menu.foreground", get.FONT_COLOR)
        UIManager.put("MenuItem.background", get.BACKGROUND_COLOR)
        UIManager.put("MenuItem.foreground", get.FONT_COLOR)
        UIManager.put("PopupMenu.background", get.BACKGROUND_COLOR)
        UIManager.put("PopupMenu.foreground", get.FONT_COLOR)
        UIManager.put("ComboBox.background", get.TEXT_FIELD_BACKGROUND_COLOR)
        UIManager.put("ComboBox.foreground", get.FONT_COLOR)
        UIManager.put("ColorChooser.background", get.BACKGROUND_COLOR)
        UIManager.put("ColorChooser.foreground", get.FONT_COLOR)
        UIManager.put("Button.background", get.BUTTON_BACKGROUND_COLOR)
        UIManager.put("Button.foreground", get.FONT_COLOR)
        UIManager.put("CheckBox.background", get.BACKGROUND_COLOR)
        UIManager.put("CheckBox.foreground", get.FONT_COLOR)
        UIManager.put("OptionPane.background", get.BACKGROUND_COLOR)
        UIManager.put("OptionPane.foreground", get.FONT_COLOR)
        UIManager.put("OptionPane.messageForeground", get.FONT_COLOR)
        UIManager.put("ScrollBar.background", get.BACKGROUND_COLOR)
        UIManager.put("ScrollBar.foreground", get.FONT_COLOR)
        UIManager.put("Separator.background", get.BACKGROUND_COLOR)
        UIManager.put("Separator.foreground", get.FONT_COLOR)
        UIManager.put("ScrollPane.background", get.BACKGROUND_COLOR)
        UIManager.put("ScrollPane.foreground", get.FONT_COLOR)
        UIManager.put("TabbedPane.background", get.LIST_BACKGROUND_COLOR)
        UIManager.put("TabbedPane.foreground", get.FONT_COLOR)
        UIManager.put("TabbedPane.selected", get.LIST_SELECTION_BACKGROUND_COLOR)
        UIManager.put("TableHeader.background", get.TABLE_HEADER_BACKGROUND_COLOR)
        UIManager.put("TableHeader.foreground", get.FONT_COLOR)
        UIManager.put("Table.background", get.TABLE_BACKGROUND_COLOR)
        UIManager.put("Table.foreground", get.FONT_COLOR)
        UIManager.put("TextArea.background", get.TEXT_FIELD_BACKGROUND_COLOR)
        UIManager.put("TextArea.foreground", get.FONT_COLOR)
        UIManager.put("TextArea.caretForeground", get.FONT_COLOR)
        UIManager.put("TextPane.background", get.TEXT_FIELD_BACKGROUND_COLOR)
        UIManager.put("TextPane.foreground", get.FONT_COLOR)
        UIManager.put("TextPane.caretForeground", get.FONT_COLOR)
        UIManager.put("TextField.background", get.TEXT_FIELD_BACKGROUND_COLOR)
        UIManager.put("TextField.foreground", get.FONT_COLOR)
        UIManager.put("TextField.caretForeground", get.FONT_COLOR)
        UIManager.put("FormattedTextField.background", get.TEXT_FIELD_BACKGROUND_COLOR)
        UIManager.put("FormattedTextField.foreground", get.FONT_COLOR)
        UIManager.put("FormattedTextField.caretForeground", get.FONT_COLOR)
        UIManager.put("TitledBorder.background", get.BACKGROUND_COLOR)
        UIManager.put("TitledBorder.foreground", get.FONT_COLOR)
        UIManager.put("ToggleButton.background", get.BACKGROUND_COLOR)
        UIManager.put("ToggleButton.foreground", get.FONT_COLOR)
        UIManager.put("ToolBar.background", get.BACKGROUND_COLOR)
        UIManager.put("ToolBar.foreground", get.FONT_COLOR)
        UIManager.put("Viewport.background", get.BACKGROUND_COLOR)
        UIManager.put("Viewport.foreground", get.FONT_COLOR)
        UIManager.put("Spinner.background", get.TEXT_FIELD_BACKGROUND_COLOR)
        UIManager.put("Spinner.foreground", get.FONT_COLOR)

        UIManager.getDefaults()["TabbedPane.contentBorderInsets"] = Insets(1, 0, 0, 0)
    }

    @Suppress("DEPRECATION")
    fun getTimerDefaultBackgroundColor(): Color {
        if (Config.timerBackgroundColor == null) {
            return get.BACKGROUND_COLOR
        }

        // Compare config color with the default color
        if (Config.timerBackgroundColor != Color(230,230,230)) {
            return Config.timerBackgroundColor!!
        }

        Config.timerBackgroundColor = null
        return get.BACKGROUND_COLOR
    }

    @Suppress("DEPRECATION")
    fun getTimerApproachingBackgroundColor(): Color {
        if (Config.approachingLimitColor == null) {
            return get.TIMER_APPROACHING_BACKGROUND_COLOR
        }

        // Compare config color with the default color
        if (Config.approachingLimitColor != Color.ORANGE) {
            return Config.approachingLimitColor!!
        }

        Config.approachingLimitColor = null
        return get.TIMER_APPROACHING_BACKGROUND_COLOR
    }

    @Suppress("DEPRECATION")
    fun getTimerExceededBackgroundColor(): Color {
        if (Config.exceededLimitColor == null) {
            return get.TIMER_EXCEEDED_BACKGROUND_COLOR
        }

        // Compare config color with the default color
        if (Config.exceededLimitColor != Color.RED) {
            return Config.exceededLimitColor!!
        }

        Config.exceededLimitColor = null
        return get.TIMER_EXCEEDED_BACKGROUND_COLOR
    }
}