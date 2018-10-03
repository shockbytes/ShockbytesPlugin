package at.shockbytes.plugin.worker

import at.shockbytes.plugin.service.android.AdbService
import at.shockbytes.plugin.service.android.CertificateService
import at.shockbytes.plugin.service.apps.AppsSyncService
import at.shockbytes.plugin.service.push.GoogleDriveOptions
import at.shockbytes.plugin.service.push.PushService
import at.shockbytes.plugin.util.ConfigManager
import at.shockbytes.plugin.view.AndroidWorkerView
import at.shockbytes.plugin.view.WorkerView
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.IconLoader
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.swing.*

class AndroidWorker(private val adbService: AdbService,
                    private val certificateService: CertificateService,
                    private val pushService: PushService,
                    private val gDriveOptions: GoogleDriveOptions) : Worker<JPanel>() {

    override val title = "Android Utilities"
    override val icon = IconLoader.getIcon("/icons/tab_android.png")
    override var view: WorkerView<JPanel> = AndroidWorkerView(this)

    private val infoSubject: PublishSubject<String> = PublishSubject.create()
    private val outputSubject: PublishSubject<String> = PublishSubject.create()
    private val ipSubject: PublishSubject<String> = PublishSubject.create()

    // --------------- Clearly separate between PublishSubject and Observable ---------------

    val outputObservable: Observable<String> = outputSubject
    val infoObservable: Observable<String> = infoSubject
    val ipObservable: Observable<String> = ipSubject

    private val appsSyncer: AppsSyncService by lazy {
        AppsSyncService(ProjectManager.getInstance().openProjects[0], pushService, gDriveOptions)
    }

    fun discoverDeviceIp() {
        adbService.discoverDeviceIp().subscribe { (msg, deviceIp) ->
            outputSubject.onNext("\n$msg\n")
            deviceIp?.let { ipSubject.onNext(it) }
        }
    }

    fun connect(deviceIp: String, port: Int) {

        adbService.connectToDevice(deviceIp, port).subscribe({ (processOutput, connectedDevice) ->
            outputSubject.onNext("$processOutput$connectedDevice\n")
            updateConnectionLabel(connectedDevice)
        }, { outputSubject.onNext("Error while connecting...\n") })
    }

    fun connectWearDevice(p: Int?) {
        p?.let { port ->
            adbService.connectToWearable(port).subscribe { output ->
                outputSubject.onNext(output)
            }
        }
    }

    fun disconnect() {
        adbService.disconnect().subscribe { output ->
            outputSubject.onNext(output)
        }
    }

    fun restartAdbServer() {
        adbService.restartAdbServer().subscribe { output ->
            outputSubject.onNext(output)
        }
    }

    private fun updateConnectionLabel(connectedDevice: String?) {

        if (!connectedDevice.isNullOrEmpty()) {

            val s = "model:"
            val len = s.length
            val info = StringBuilder()
            var endIdx = -1
            var startIdx: Int

            do {
                startIdx = connectedDevice!!.indexOf(s, endIdx + 1)
                if (startIdx > -1) {
                    endIdx = connectedDevice.indexOf(" ", startIdx + 1)
                    val device = connectedDevice.substring(startIdx + len, endIdx)
                    info.append(device).append("     ")
                }
            } while (startIdx > -1)

            infoSubject.onNext("Connected device(s): $info")
        }
    }

    fun sendApkToDevice() {
        appsSyncer.tryCopyDebugAPK().subscribe({
            // Do nothing here...
        }, { throwable ->
            outputSubject.onNext("Error while sending apk to device (" + throwable.message + ")...\n")
        })
    }

    fun showDebugCertificateInformation() {
        certificateService.getDebugCertificate(ConfigManager.loadDebugCertificatePath())
                .subscribe { certInfo -> outputSubject.onNext(certInfo) }
    }

    fun showCustomCertificateInformation() {
        certificateService.getCustomCertificate(ConfigManager.loadCustomCertificates()[0]) // TODO Update UI to handle multiple certs
                .subscribe { certInfo -> outputSubject.onNext(certInfo) }
    }

}
