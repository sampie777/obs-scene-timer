package objects

import nl.sajansen.obsscenetimer.objects.TScene
import nl.sajansen.obsscenetimer.objects.TSource
import nl.sajansen.obsscenetimer.objects.TVideoFile
import kotlin.test.Test
import kotlin.test.assertEquals

class TSceneTest {

    @Test
    fun testMaxVideoLengthWithoutVideoSources() {
        val scene = TScene()
        val sources: ArrayList<TSource> = ArrayList()
        scene.sources = sources

        sources.add(TSource())
        sources.add(TSource())
        sources.add(TSource())

        assertEquals(0, scene.maxVideoLength())
    }

    @Test
    fun testMaxVideoLengthWithVideoSources() {
        val scene = TScene()
        val sources: ArrayList<TSource> = ArrayList()
        scene.sources = sources

        sources.add(TSource())
        sources.add(TSource())
        sources.add(TSource())

        sources[0].file = TVideoFile(duration = 15)
        sources[1].file = TVideoFile(duration = 91)
        sources[2].file = TVideoFile(duration = 0)

        assertEquals(91, scene.maxVideoLength())
    }
}