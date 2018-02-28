package at.shockbytes.plugin.general

import com.intellij.openapi.util.IconLoader
import org.junit.Test
import kotlin.test.assertNotNull


class ResourcesTest {

    @Test
    fun testIconLoading() {

        val url = ResourcesTest::class.java.getResource("/icons/ic_add.png")
        assertNotNull(url)

        val icon = IconLoader.getIcon("/icons/ic_add.png")
        assertNotNull(icon)
    }

}