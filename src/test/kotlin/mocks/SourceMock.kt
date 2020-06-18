package mocks


import net.twasi.obsremotejava.objects.Source

class SourceMock(private val name: String) : Source() {
    override fun getName(): String = name
    override fun getType(): String = ""
}