package at.shockbytes.plugin.service.workspace

import java.io.File

interface WorkspaceFileFilter {

    fun filter(f: File, keyword: String, crawlOptions: CrawlOptions): Boolean

}