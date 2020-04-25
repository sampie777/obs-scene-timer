package themes

import config.Config
import java.util.logging.Logger
import javax.swing.UIManager

object Theme {
    private val logger = Logger.getLogger(Theme::class.java.name)

    lateinit var get: DefaultTheme

    init {
        set(Config.theme)
    }

    fun init() {
        set(Config.theme)
        apply()
    }

    fun set(theme: String) {
        val themeClassName = theme + "Theme"

        get = when (themeClassName) {
            "DarkTheme" -> DarkTheme()
            "DefaultTheme" -> DefaultTheme()
            else -> {
                logger.warning("Could not find theme '$theme'. Using default theme.")
                DefaultTheme()
            }
        }
    }

    fun availableThemes(): List<String> {
        return listOf("Default", "Dark")
    }

    private fun apply() {
        UIManager.put("Panel.background", get.BACKGROUND_COLOR)
        UIManager.put("Panel.foreground", get.FONT_COLOR)
        UIManager.put("SplitPaneDivider.draggingColor", get.BACKGROUND_COLOR)
        UIManager.put("SplitPane.background", get.BACKGROUND_COLOR)
        UIManager.put("Label.background", get.BACKGROUND_COLOR)
        UIManager.put("Label.foreground", get.FONT_COLOR)
        UIManager.put("List.background", get.BACKGROUND_COLOR)
        UIManager.put("List.foreground", get.FONT_COLOR)
        UIManager.put("MenuBar.background", get.BACKGROUND_COLOR)
        UIManager.put("MenuBar.foreground", get.FONT_COLOR)
        UIManager.put("MenuBar.borderColor", get.MENU_BAR_BORDER_COLOR)
        UIManager.put("Menu.background", get.BACKGROUND_COLOR)
        UIManager.put("Menu.foreground", get.FONT_COLOR)
        UIManager.put("MenuItem.background", get.BACKGROUND_COLOR)
        UIManager.put("MenuItem.foreground", get.FONT_COLOR)
        UIManager.put("ComboBox.background", get.TEXT_FIELD_BACKGROUND_COLOR)
        UIManager.put("ComboBox.foreground", get.FONT_COLOR)
        UIManager.put("ColorChooser.background", get.BACKGROUND_COLOR)
        UIManager.put("ColorChooser.foreground", get.FONT_COLOR)
        UIManager.put("Button.background", get.BACKGROUND_COLOR)
        UIManager.put("Button.foreground", get.FONT_COLOR)
        UIManager.put("CheckBox.background", get.BACKGROUND_COLOR)
        UIManager.put("CheckBox.foreground", get.FONT_COLOR)
        UIManager.put("OptionPane.background", get.BACKGROUND_COLOR)
        UIManager.put("OptionPane.foreground", get.FONT_COLOR)
        UIManager.put("ScrollBar.background", get.BACKGROUND_COLOR)
        UIManager.put("ScrollBar.foreground", get.FONT_COLOR)
        UIManager.put("Separator.background", get.BACKGROUND_COLOR)
        UIManager.put("Separator.foreground", get.FONT_COLOR)
        UIManager.put("ScrollPane.background", get.BACKGROUND_COLOR)
        UIManager.put("ScrollPane.foreground", get.FONT_COLOR)
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
    }
}