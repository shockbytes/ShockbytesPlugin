package at.shockbytes.plugin.platform

import at.shockbytes.plugin.service.android.*
import at.shockbytes.plugin.service.push.GoogleDriveOptions
import at.shockbytes.plugin.service.push.GooglePushService
import at.shockbytes.plugin.service.push.PushService
import at.shockbytes.plugin.service.workspace.DefaultWorkspaceCrawler
import at.shockbytes.plugin.service.workspace.MacOsWorkspaceFileFilter
import at.shockbytes.plugin.service.workspace.WorkspaceCrawler
import at.shockbytes.plugin.util.ConfigManager

class MacOsPlatformManager : PlatformManager {

    override val adbService: AdbService = MacOsAdbService()

    override val certificateService: CertificateService = KeyStoreBackedCertificateService()

    override val pushService: PushService = GooglePushService(ConfigManager.loadFCMApiKey())

    override val workSpaceCrawler: WorkspaceCrawler = DefaultWorkspaceCrawler(ConfigManager.loadWorkspaceLocation(), MacOsWorkspaceFileFilter())

    override val googleDriveOptions: GoogleDriveOptions = GoogleDriveOptions(
            "/Users/martinmacheiner/Google Drive/apps/",
            "Users/martinmacheiner/Google Drive/apps_fcm_token.txt")

}
