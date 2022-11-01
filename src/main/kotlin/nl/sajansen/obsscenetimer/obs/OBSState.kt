package nl.sajansen.obsscenetimer.obs

import nl.sajansen.obsscenetimer.objects.TScene

object OBSState {
    var currentScene: TScene = TScene("No scene selected")
    val scenes: ArrayList<TScene> = ArrayList()

    var clientActivityStatus: OBSClientStatus? = null
    var connectionStatus: OBSConnectionStatus = OBSConnectionStatus.UNKNOWN
}