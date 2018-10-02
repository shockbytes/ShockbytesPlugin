package at.shockbytes.plugin.platform

import at.shockbytes.plugin.service.android.AdbService
import at.shockbytes.plugin.service.android.CertificateService
import at.shockbytes.plugin.service.push.GoogleDriveOptions
import at.shockbytes.plugin.service.push.PushService
import at.shockbytes.plugin.service.workspace.WorkspaceCrawler

interface PlatformManager {

    val adbService: AdbService

    val certificateService: CertificateService

    val pushService: PushService

    val workSpaceCrawler: WorkspaceCrawler

    val googleDriveOptions: GoogleDriveOptions

    companion object {

        fun forPlatform(platform: String): PlatformManager {
            return when {
                platform.toLowerCase().contains("win") -> WindowsPlatformManager()
                platform.toLowerCase().contains("mac") -> MacOsPlatformManager()
                else -> throw IllegalArgumentException("$platform not supported!")
            }

        }
    }

}