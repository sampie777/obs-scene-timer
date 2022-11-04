package nl.sajansen.obsscenetimer.objects


data class TSource(
    var name: String = "",
    var kind: String = "unknown",
    var file: TVideoFile? = null,
    var playlist: TPlayList? = null,
    var settingsAreLoading: Boolean = false,
    var settingsLoaded: Boolean = false,
) {
    fun copyTo(source: TSource) {
        source.name = this.name
        source.kind = this.kind
        source.file = this.file
        source.playlist = this.playlist
        source.settingsAreLoading = this.settingsAreLoading
        source.settingsLoaded = this.settingsLoaded
    }
}
