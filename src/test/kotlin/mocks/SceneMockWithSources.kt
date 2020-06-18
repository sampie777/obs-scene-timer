package mocks


import net.twasi.obsremotejava.objects.Scene
import net.twasi.obsremotejava.objects.Source

class SceneMockWithSources(private val name: String) : Scene() {
    override fun getName(): String = name
    override fun getSources(): List<Source> {
        return listOf(SourceMock("$name source"))
    }
}