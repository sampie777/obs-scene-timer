package nl.sajansen.obsscenetimer.mocks

import io.obswebsocket.community.client.model.Source

class SourceMock(private val name: String) : Source() {
    override fun getSourceName(): String = name
    override fun getSourceType(): String = "OBS_SOURCE_TYPE_INPUT"
    override fun getInputKind(): String = ""
}