package at.shockbytes.plugin.platform

import at.shockbytes.plugin.service.android.AdbService
import at.shockbytes.plugin.service.android.CertificateService
import at.shockbytes.plugin.service.android.KeyStoreBackedCertificateService
import at.shockbytes.plugin.service.android.WindowsAdbService
import at.shockbytes.plugin.service.push.GoogleDriveOptions
import at.shockbytes.plugin.service.push.GooglePushService
import at.shockbytes.plugin.service.push.PushService
import at.shockbytes.plugin.service.workspace.DefaultWorkspaceCrawler
import at.shockbytes.plugin.service.workspace.WindowsWorkspaceFileFilter
import at.shockbytes.plugin.service.workspace.WorkspaceCrawler
import at.shockbytes.plugin.util.ConfigManager

class WindowsPlatformManager : PlatformManager {

    override val adbService: AdbService = WindowsAdbService()

    override val certificateService: CertificateService = KeyStoreBackedCertificateService()

    override val pushService: PushService = GooglePushService(ConfigManager.loadFCMApiKey())

    override val workSpaceCrawler: WorkspaceCrawler = DefaultWorkspaceCrawler(ConfigManager.loadWorkspaceLocation(), WindowsWorkspaceFileFilter())

    override val googleDriveOptions: GoogleDriveOptions = GoogleDriveOptions(
            "C:/Users/Mescht/Google Drive/apps/",
            "C:/Users/Mescht/Google Drive/apps_fcm_token.txt")

}
