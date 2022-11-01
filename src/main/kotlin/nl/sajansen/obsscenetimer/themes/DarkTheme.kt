package nl.sajansen.obsscenetimer.themes

import java.awt.Color

open class DarkTheme : BaseTheme() {
    override val FONT_FAMILY = "Dialog"
    override val FONT_COLOR = Color(200, 200, 200)
    override val LINK_FONT_COLOR = Color(106, 149, 239)
    override val BACKGROUND_COLOR = Color(50, 50, 50)
    override val TEXT_FIELD_BACKGROUND_COLOR = Color(78, 78, 78)
    override val LIST_BACKGROUND_COLOR = Color(70, 70, 70)
    override val BUTTON_BACKGROUND_COLOR = Color(65, 65, 65)
    override val TABLE_HEADER_BACKGROUND_COLOR = Color(50, 50, 50)
    override val TABLE_BACKGROUND_COLOR = Color(78, 78, 78)

    override val BORDER_COLOR = Color(130, 130, 130)
    override val MENU_BAR_BORDER_COLOR = Color(65, 65, 65)

    override val LIST_SELECTION_FONT_COLOR_LIGHT = Color(200, 200, 200)
    override val LIST_SELECTION_FONT_COLOR_DARK = Color(51, 51, 51)
    override val LIST_SELECTION_BACKGROUND_COLOR = Color(79, 84, 90)
    override val LIST_SELECTION_FONT_COLOR_DEFAULT = LIST_SELECTION_FONT_COLOR_LIGHT

    override val TIMER_APPROACHING_FONT_COLOR = Color(51, 51, 51)
    override val TIMER_APPROACHING_BACKGROUND_COLOR = Color(201, 127, 0)
    override val TIMER_EXCEEDED_FONT_COLOR = Color(51, 51, 51)
    override val TIMER_EXCEEDED_BACKGROUND_COLOR = Color(255, 0, 0)

    override val NOTIFICATIONS_BUTTON_ICON_DEFAULT = "/nl/sajansen/obsscenetimer/notification-bell-empty-inverted-24.png"
    override val NOTIFICATIONS_BUTTON_ICON_ALERT ="/nl/sajansen/obsscenetimer/notification-bell-yellow-inverted-24.png"
}