package at.shockbytes.plugin.gradle

import at.shockbytes.plugin.util.GradleDependencyInjector
import com.intellij.openapi.project.Project
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import kotlin.test.assertTrue

/**
 * This isn't a plan JUnit test, as it interacts with the file system, but
 * it is a cheap way to easily verify if the class under test behaves the right
 * way (instead of spawning a new IntelliJ instance and test it there)
 */
class GradleWorkerTest {

    private lateinit var project: Project

    @Before
    fun setup() {

        project = Mockito.mock(Project::class.java)
        Mockito.`when`(project.name).thenReturn("PluginTestProject")
    }

    @Test
    fun testDependencyInjectorGrabImportantFiles() {

        val rootFolder = "C:/Users/Mescht/AndroidStudioProjects/PluginTestProject"
        val injector = GradleDependencyInjector(project, rootFolder, false)
        val verified = injector.verifyFiles()

        assertTrue(verified)
    }


}