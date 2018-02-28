package at.shockbytes.plugin.dependency

import at.shockbytes.plugin.service.dependency.GradleDependencyResolveService
import at.shockbytes.plugin.service.dependency.model.GradleDependency
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DependencyResolverTest {

    private lateinit var service: GradleDependencyResolveService

    @Before
    fun setup() {
        service = GradleDependencyResolveService()
    }

    @Test
    fun testMavenCentralResolver() {

        val dep = GradleDependency("Google Gson", "implementation 'com.google.code.gson:gson:_VERSION_'",
                "", "g:com.google.code.gson+AND+a:gson", GradleDependency.EndPoint.MAVEN_CENTRAL)
        val resolvedDep = service.resolveDependencyVersion(dep, dep.endPoint).blockingFirst()

        assertEquals("2.8.2", resolvedDep.version)
    }

    @Test
    fun testJitpackIoResolver() {

        val dep = GradleDependency("Shockbytes ShockUtil", "implementation 'com.github.shockbytes:ShockUtil:_VERSION_'",
                "", "com.github.shockbytes/ShockUtil", GradleDependency.EndPoint.JITPACK_IO)
        val resolvedDep = service.resolveDependencyVersion(dep, dep.endPoint).blockingFirst()

        assertEquals("2.1.0", resolvedDep.version)
    }

    @Test
    fun testClojarsResolver() {

        val dep = GradleDependency("Icepick", "implementation \"frankiesardo:icepick:_VERSION_\"",
                "", "frankiesardo/icepick", GradleDependency.EndPoint.CLOJARS)
        val resolvedDep = service.resolveDependencyVersion(dep, dep.endPoint).blockingFirst()

        assertEquals("3.2.0", resolvedDep.version)
        println(resolvedDep)
    }

    @Test
    fun testReplaceVersion() {

        val t = "implementation 'com.google.android.gms:play-services-drive:_VERSION_'"
        val replaced = t.replace("_VERSION_", "11.2.0")

        assertEquals("implementation 'com.google.android.gms:play-services-drive:11.2.0'", replaced)
    }

    @Test
    fun testReplaceMultiLineVersion() {

        val t = "implementation 'frankiesardo:icepick:_VERSION_'\nkapt 'frankiesardo:icepick-processor:_VERSION_'"
        val replaced = t.replace("_VERSION_", "3.2.0")

        assertEquals("implementation 'frankiesardo:icepick:3.2.0'\nkapt 'frankiesardo:icepick-processor:3.2.0'", replaced)
    }

}