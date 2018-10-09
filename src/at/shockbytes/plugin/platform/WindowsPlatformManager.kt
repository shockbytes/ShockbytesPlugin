package at.shockbytes.plugin.platform

import at.shockbytes.plugin.service.android.AdbService
import at.shockbytes.plugin.service.android.CertificateService
import at.shockbytes.plugin.service.android.KeyStoreBackedCertificateService
import at.shockbytes.plugin.service.android.DefaultAdbService
import at.shockbytes.plugin.service.process.DefaultProcessExecutionService
import at.shockbytes.plugin.service.process.ProcessExecutionService
import at.shockbytes.plugin.service.push.GoogleDriveOptions
import at.shockbytes.plugin.service.push.GooglePushService
import at.shockbytes.plugin.service.push.PushService
import at.shockbytes.plugin.service.workspace.DefaultWorkspaceCrawler
import at.shockbytes.plugin.service.workspace.WindowsWorkspaceFileFilter
import at.shockbytes.plugin.service.workspace.WorkspaceCrawler
import at.shockbytes.plugin.util.ConfigManager

class WindowsPlatformManager : PlatformManager {

    override val processExecutionService: ProcessExecutionService = DefaultProcessExecutionService()

    override val adbService: AdbService = DefaultAdbService(processExecutionService)

    override val certificateService: CertificateService = KeyStoreBackedCertificateService()

    override val pushService: PushService = GooglePushService(ConfigManager.loadFCMApiKey())

    override val workSpaceCrawler: WorkspaceCrawler = DefaultWorkspaceCrawler(ConfigManager.loadWorkspaceLocation(), WindowsWorkspaceFileFilter())

    override val googleDriveOptions: GoogleDriveOptions = GoogleDriveOptions(
            "${System.getProperty("user.home")}/Google Drive/apps/",
            "${System.getProperty("user.home")}/Google Drive/apps_fcm_token.txt")

}
