package at.shockbytes.plugin.service.workspace

import io.reactivex.Single

interface WorkspaceCrawler {

    fun crawl(keyword: String, crawlOptions: CrawlOptions): Single<Pair<String, Array<String>>>
}