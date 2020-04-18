package objects

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

        sources[0].videoLength = 15
        sources[1].videoLength = 91
        sources[2].videoLength = 0

        assertEquals(91, scene.maxVideoLength())
    }
}