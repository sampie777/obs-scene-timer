package themes

import java.awt.Color

open class DarkTheme : DefaultTheme() {
    override val FONT_FAMILY = "Dialog"
    override val FONT_COLOR = Color(200, 200, 200)
    override val LINK_FONT_COLOR = Color(106, 149, 239)
    override val BACKGROUND_COLOR = Color(50, 50, 50)
    override val TEXT_FIELD_BACKGROUND_COLOR = Color(78, 78, 78)

    override val BORDER_COLOR = Color(130, 130, 130)
    override val MENU_BAR_BORDER_COLOR = Color(65, 65, 65)

    override val TIMER_APPROACHING_FONT_COLOR = Color(51, 51, 51)
    override val TIMER_APPROACHING_BACKGROUND_COLOR = Color(201, 127, 0)
    override val TIMER_EXCEEDED_FONT_COLOR = Color(51, 51, 51)
    override val TIMER_EXCEEDED_BACKGROUND_COLOR = Color(255, 0, 0)

    override val NOTIFICATIONS_BUTTON_ICON_DEFAULT = "/notification-bell-empty-inverted-24.png"
    override val NOTIFICATIONS_BUTTON_ICON_ALERT ="/notification-bell-yellow-inverted-24.png"
}