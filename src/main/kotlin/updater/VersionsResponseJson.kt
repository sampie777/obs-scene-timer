package updater

data class VersionsResponseVersionsJson(val name: String)

data class VersionsResponseJson(val values: List<VersionsResponseVersionsJson>)