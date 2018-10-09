package at.shockbytes.plugin.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectRootManager
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.BiPredicate
import java.util.stream.Collectors

/**
 * Author:  Martin Macheiner
 * Date:    03.08.2016
 */
object IdeaProjectUtils {

    fun getPackagesFromProject(project: Project): List<String> {

        val vFiles = ProjectRootManager.getInstance(project).contentSourceRoots
        vFiles.forEach { f ->
            try {
                return Files.find(Paths.get(f.path), 999, BiPredicate { _, bfa -> bfa.isDirectory })
                        .map { p -> p.toFile().absolutePath.replace("\\", "/").replace(f.path, "") }
                        .filter { s -> !s.isEmpty() && StringUtils.countMatches(s, "/") > 3 }
                        .collect(Collectors.toList())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return listOf()
    }

    fun getDisplayablePackageName(packages: List<String>): List<String> {
        return packages.map { s -> s.substring(StringUtils.ordinalIndexOf(s, "/", 4)) }
    }

    fun getSourceRootFolder(project: Project): String? {
        val vFiles = ProjectRootManager.getInstance(project).contentSourceRoots
        return if (vFiles.isNotEmpty()) {
            vFiles[0].path
        } else null
    }

    fun getProjectRootFolder(project: Project): String? {
        return project.basePath
    }

    fun getOpenProject(): Project = ProjectManager.getInstance().openProjects[0]

    fun getOpenedProjectRootFolder(): String? = getOpenProject().basePath

}
