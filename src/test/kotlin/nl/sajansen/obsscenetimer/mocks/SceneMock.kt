package nl.sajansen.obsscenetimer.mocks

import io.obswebsocket.community.client.model.Scene


class SceneMock(private val name: String, private val index: Int = 0) : Scene(name, index) {
    override fun getSceneName(): String = name
    override fun getSceneIndex(): Int = index
}