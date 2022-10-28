package objects


class TSource {
    var name = ""
    var kind = "unknown"
    var file: TVideoFile? = null
    var playlist: TPlayList? = null
    var settingsLoaded = false

    constructor()

    constructor(name: String) {
        this.name = name
    }

    constructor(name: String, kind: String) {
        this.name = name
        this.kind = kind
    }

    fun copyTo(source: TSource) {
        source.name = this.name
        source.kind = this.kind
        source.file = this.file
        source.settingsLoaded = this.settingsLoaded
    }
}
