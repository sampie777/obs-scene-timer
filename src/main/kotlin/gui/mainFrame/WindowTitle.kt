package gui.mainFrame

import config.Config
import getTimeAsClock
import objects.OBSSceneTimer
import obs.OBSState


object WindowTitle {
    const val VARIABLE_IDENTIFIER = "{%s}"
    const val SCENE_NAME = "sceneName"
    const val TIMER_ELAPSED = "timerElapsed"
    const val TIMER_REMAINING = "timerRemaining"
    const val SCENE_TIME_LIMIT = "sceneLimit"

    fun variables(): List<String> {
        return listOf(
            SCENE_NAME,
            TIMER_ELAPSED,
            TIMER_REMAINING,
            SCENE_TIME_LIMIT
        )
    }

    fun generateWindowTitle(): String {
        return Config.mainWindowTitle
            .replace(VARIABLE_IDENTIFIER.format(SCENE_NAME), OBSState.currentScene.name)
            .replace(VARIABLE_IDENTIFIER.format(TIMER_ELAPSED), OBSSceneTimer.getTimerAsClock())
            .replace(
                VARIABLE_IDENTIFIER.format(TIMER_REMAINING),
                if ((OBSSceneTimer.getMaxTimerValue() > 0))
                    getTimeAsClock(OBSSceneTimer.getMaxTimerValue() - OBSSceneTimer.getValue())
                else ""
            )
            .replace(VARIABLE_IDENTIFIER.format(SCENE_TIME_LIMIT), getTimeAsClock(OBSSceneTimer.getMaxTimerValue()))
    }
}