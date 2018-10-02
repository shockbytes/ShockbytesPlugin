package at.shockbytes.plugin.service.workspace

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.asSequence

/**
 * Author:  Martin Macheiner
 * Date:    16.03.2018
 */
class DefaultWorkspaceCrawler(private val workspaceDirectory: String,
                              private val fileFilter: WorkspaceFileFilter) : WorkspaceCrawler {

    override fun crawl(keyword: String, crawlOptions: CrawlOptions): Single<Pair<String, Array<String>>> {
        return Single.fromCallable {
            val files = Files.walk(Paths.get(workspaceDirectory))
                    .parallel()
                    .asSequence()
                    .filter { fileFilter.filter(it.toFile(), keyword.toLowerCase(), crawlOptions) }
                    .map { p -> p.toAbsolutePath().toString() }
                    .toList()
                    .toTypedArray()
            Pair(keyword, files)
        }.subscribeOn(Schedulers.io())
    }

}