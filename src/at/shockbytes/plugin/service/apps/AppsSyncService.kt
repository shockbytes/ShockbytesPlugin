package at.shockbytes.plugin.service.apps

import at.shockbytes.plugin.model.AppsSyncState
import at.shockbytes.plugin.service.push.GooglePushService
import at.shockbytes.plugin.service.push.PushService
import at.shockbytes.plugin.util.HelperUtil
import at.shockbytes.plugin.util.ShockConfig
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import org.apache.commons.io.IOUtils
import java.io.*
import java.util.*
import javax.swing.JTextArea

/**
 * Author:  Martin Macheiner
 * Date:    17.04.2017
 */
class AppsSyncService(private val project: Project) {

    private val projectName: String = project.name
    private val outputDirectory: String = project.basePath + "/app/build/outputs/apk"
    private var deviceToken: String? = null


    private var syncState: AppsSyncState? = null

    private val pushManager: PushService

    init {
        pushManager = GooglePushService(ShockConfig.loadFCMApiKey())
        initializeAppsSyncState()
    }

    @Throws(IOException::class)
    fun tryCopyDebugAPK(outputArea: JTextArea) {

        val f = File(outputDirectory + "/app-debug.apk")
        if (!f.exists()) {
            throw FileNotFoundException("APK not generated!")
        }

        deviceToken = grabTokenFromDrive()
        if (deviceToken == null) {
            throw IllegalStateException("No token stored in Google Drive file apps_fcm_token.txt!")
        }

        copyToGoogleDrive(projectName, f.absolutePath, outputArea)
    }

    private fun initializeAppsSyncState() {

        syncState = ServiceManager.getService(AppsSyncState::class.java)
        if (syncState == null) {
            syncState = AppsSyncState()
        }
    }

    private fun grabTokenFromDrive(): String? {

        var token: String? = null
        val tokenFile = File(GOOGLE_DRIVE_FCM_TOKEN_PATH)
        try {
            InputStreamReader(FileInputStream(tokenFile)).use { `in` -> token = IOUtils.readLines(`in`)[0] }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return token
    }

    private fun copyToGoogleDrive(name: String, inApk: String, outputArea: JTextArea) {

        //String inIconPath = outputDirectory + "/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png";
        //String outIconPath = GOOGLE_DRIVE_PATH + name + ".png";

        val revision = revisionLookup(name)
        val outApkPath = GOOGLE_DRIVE_PATH + name + "_" + revision + ".apk"

        try {

            // Delete all old revisions in Google Drive first
            cleanupOldRevisions(name)

            // Copy APK
            HelperUtil.copyFile(inApk, outApkPath)

            // Copy icon
            //HelperUtil.copyFile(inIconPath, outIconPath);

            val title = name + " ready to update"
            val body = "A new revision is available"

            outputArea.append("$name Rev. $revision copied to destination folder\n")

            val t = Timer()
            t.schedule(object : TimerTask() {
                override fun run() {
                    if (pushManager.sendToDevice(deviceToken!!, title, body)) {
                        showBalloonMessage("Shockbytes Apps", "Sync $name Rev. $revision",
                                "Syncing $name.apk with Google Drive")
                        //HelperUtil.deleteFile(new File(inApk));
                    }
                }
            }, 5000)

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun revisionLookup(name: String): Int {

        var rev = syncState?.getRevisionForApp(name)
        // No revision available, now add it
        if (rev == null) {
            syncState?.addApp(name)
            return 1
        } else {
            syncState?.incrementRevisionForApp(name)
            rev++
        }
        return rev
    }

    private fun cleanupOldRevisions(name: String) {

        val out = File(GOOGLE_DRIVE_PATH)
        val files = out.list()
        files
                ?.filter { it.startsWith(name + "_") && it.endsWith(".apk") }
                ?.forEach { HelperUtil.deleteFile(File(GOOGLE_DRIVE_PATH + it)) }
    }

    private fun showBalloonMessage(title: String, subtitle: String, content: String) {
        ApplicationManager.getApplication().invokeLater {
            val notification = GROUP_DISPLAY_ID_INFO
                    .createNotification(title, subtitle, content, NotificationType.INFORMATION)
            Notifications.Bus.notify(notification, project)
        }
    }

    companion object {

        private val GROUP_DISPLAY_ID_INFO = NotificationGroup("Shockbytes Apps",
                NotificationDisplayType.BALLOON, true, null,
                IconLoader.getIcon("/icons/ic_google_drive.png"))

        private const val GOOGLE_DRIVE_PATH = "C:/Users/Mescht/Google Drive/apps/"
        private const val GOOGLE_DRIVE_FCM_TOKEN_PATH = "C:/Users/Mescht/Google Drive/apps_fcm_token.txt"

    }

}
