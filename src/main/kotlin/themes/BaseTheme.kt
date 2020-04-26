package themes

import java.awt.Color

abstract class BaseTheme {
    open val FONT_FAMILY = "Dialog"
    open val FONT_COLOR = Color(51, 51, 51)
    open val LINK_FONT_COLOR = Color(25, 90, 244)
    open val BACKGROUND_COLOR = Color(238, 238, 238)
    open val TEXT_FIELD_BACKGROUND_COLOR = Color(255, 255, 255)

    open val BORDER_COLOR = Color(168, 168, 168)
    open val MENU_BAR_BORDER_COLOR = Color(204, 204, 204)

    open val TIMER_APPROACHING_FONT_COLOR = Color(51, 51, 51)
    open val TIMER_APPROACHING_BACKGROUND_COLOR = Color.ORANGE
    open val TIMER_EXCEEDED_FONT_COLOR = Color(51, 51, 51)
    open val TIMER_EXCEEDED_BACKGROUND_COLOR = Color.RED

    open val NOTIFICATIONS_BUTTON_ICON_DEFAULT = "/notification-bell-empty-24.png"
    open val NOTIFICATIONS_BUTTON_ICON_ALERT ="/notification-bell-yellow-24.png"
}