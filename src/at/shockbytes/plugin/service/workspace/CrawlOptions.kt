package at.shockbytes.plugin.service.workspace

data class CrawlOptions(val excludeBinaries: Boolean,
                        val excludeProjectFiles: Boolean,
                        val excludeGenerated: Boolean)