package objects

object Globals {
    val scenes: HashMap<String, TScene> = HashMap()
    var OBSStatus: OBSStatus? = null
    var OBSConnectionStatus: OBSStatus = objects.OBSStatus.UNKNOWN
}