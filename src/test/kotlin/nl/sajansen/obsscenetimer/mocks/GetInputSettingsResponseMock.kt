package nl.sajansen.obsscenetimer.mocks

import com.google.gson.JsonObject
import io.obswebsocket.community.client.message.response.inputs.GetInputSettingsResponse

class GetInputSettingsResponseMock(
    private val inputKind: String = "",
    private val isSuccessfulValue: Boolean = true,
    private val inputSettings: JsonObject? = null
) : GetInputSettingsResponse() {
    override fun getInputKind() = inputKind
    override fun isSuccessful() = isSuccessfulValue
    override fun getInputSettings() = inputSettings
}