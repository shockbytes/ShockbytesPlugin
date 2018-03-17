package at.shockbytes.plugin.service.workspace

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

/**
 * Author:  Martin Macheiner
 * Date:    16.03.2018
 */
class DefaultWorkspaceCrawler(private val workspaceDirectory: String) : WorkspaceCrawler {

    override fun crawl(keyword: String, crawlOptions: CrawlOptions): Single<Pair<String, Array<String>>> {
        return Single.fromCallable {
            val files = Files.walk(Paths.get(workspaceDirectory))
                    .parallel()
                    .filter { fileFilter(it.toFile(), keyword.toLowerCase(), crawlOptions) }
                    .map { p -> p.toAbsolutePath().toString() }
                    .collect(Collectors.toList())
                    .toTypedArray()
            Pair(keyword, files)
        }.subscribeOn(Schedulers.io())
    }

    private fun fileFilter(f: File, keyword: String, crawlOptions: CrawlOptions): Boolean {

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