package at.shockbytes.plugin

import at.shockbytes.plugin.dependency.DependencyResolverTest
import at.shockbytes.plugin.general.ResourcesTest
import at.shockbytes.plugin.gradle.GradleWorkerTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(DependencyResolverTest::class, GradleWorkerTest::class, ResourcesTest::class)
class AllTests