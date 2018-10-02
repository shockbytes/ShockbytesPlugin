package at.shockbytes.plugin.service.apps

import at.shockbytes.plugin.model.AppsSyncState
import at.shockbytes.plugin.service.push.GoogleDriveOptions
import at.shockbytes.plugin.service.push.PushService
import at.shockbytes.plugin.util.IOUtils
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.util.*
import javax.swing.JTextArea

/**
 * Author:  Martin Macheiner
 * Date:    17.04.2017
 */
class AppsSyncService(private val project: Project,
                      private val pushManager: PushService,
                      private val googleDriveOptions: GoogleDriveOptions) {

    private val projectName: String = project.name
    private val outputDirectory: String = project.basePath + "/app/build/outputs/apk"
    private var deviceToken: String? = null


    private var syncState: AppsSyncState? = null

    init {
        initializeAppsSyncState()
    }

    fun tryCopyDebugAPK(outputArea: JTextArea): Completable {
        return Completable.fromAction {
            val f = File("$outputDirectory/app-debug.apk")
            if (!f.exists()) {
                throw FileNotFoundException("APK not generated!")
            }

            deviceToken = grabTokenFromDrive()
            if (deviceToken == null) {
                throw IllegalStateException("No token stored in Google Drive file apps_fcm_token.txt!")
            }

            copyToGoogleDrive(projectName, f.absolutePath, outputArea)
        }.subscribeOn(Schedulers.io())
    }

    private fun initializeAppsSyncState() {

        syncState = ServiceManager.getService(AppsSyncState::class.java)
        if (syncState == null) {
            syncState = AppsSyncState()
        }
    }

    private fun grabTokenFromDrive(): String? {

        val tokenFile = File(googleDriveOptions.fcmTokenSyncPath)
        return try {
            InputStreamReader(FileInputStream(tokenFile)).use { return org.apache.commons.io.IOUtils.readLines(it)[0] }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun copyToGoogleDrive(name: String, inApk: String, outputArea: JTextArea) {

        val revision = revisionLookup(name)
        val outApkPath = "${googleDriveOptions.drivePathApps}${name}_$revision.apk"

        // Delete all old revisions in Google Drive first
        cleanupOldRevisions(name)
        // Copy APK
        IOUtils.copyFile(inApk, outApkPath)

        val title = "$name ready to update"
        val body = "A new revision is available"

        outputArea.append("$name Rev. $revision copied to destination folder\n")

        val t = Timer()
        t.schedule(object : TimerTask() {
            override fun run() {
                if (pushManager.sendToDevice(deviceToken!!, title, body)) {
                    showBalloonMessage("Shockbytes Apps", "Sync $name Rev. $revision",
                            "Syncing $name.apk with Google Drive")
                    //IdeaProjectUtils.deleteFile(new File(inApk));
                }
            }
        }, 5000)
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
        File(googleDriveOptions.drivePathApps).list()
                .filter { it.startsWith("${name}_") && it.endsWith(".apk") }
                .forEach { IOUtils.deleteFile(File("${googleDriveOptions.drivePathApps}$it")) }
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
    }

}
