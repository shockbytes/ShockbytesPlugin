package at.shockbytes.plugin.service.workspace

import java.io.File
import java.nio.file.Files

class WindowsWorkspaceFileFilter: WorkspaceFileFilter {

    override fun filter(f: File, keyword: String, crawlOptions: CrawlOptions): Boolean {

        val n = f.name.toLowerCase()
        return when {

            // Check if keyword is in path and it is not a directory
            (!n.contains(keyword) || Files.isDirectory(f.toPath())) -> false

            // Check for binary files
            crawlOptions.excludeBinaries && (n.endsWith(".class") || n.endsWith(".exe")
                    || n.endsWith(".jar") || n.endsWith(".bat") || n.endsWith(".dex")) -> false

            // Check for project files
            crawlOptions.excludeProjectFiles && (n.endsWith(".dependency") || n.endsWith(".xml")
                    || n.endsWith(".iml") || n.endsWith(".properties")) -> false

            // Check for generated files
            crawlOptions.excludeGenerated && (n.contains("$\$ViewBinder")
                    || f.absolutePath.contains("build\\generated\\source\\kapt")
                    || f.absolutePath.contains("build\\generated\\source\\apt")
                    || f.absolutePath.contains("build\\intermediates\\res\\merged")
                    || f.absolutePath.contains("build\\tmp\\kapt3\\stubs")) -> false

            else -> true
        }
    }

}
