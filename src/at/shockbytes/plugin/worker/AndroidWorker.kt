package at.shockbytes.plugin.worker

import at.shockbytes.plugin.service.android.AdbService
import at.shockbytes.plugin.service.android.CertificateService
import at.shockbytes.plugin.service.android.KeyStoreBackedCertificateService
import at.shockbytes.plugin.service.android.WindowsAdbService
import at.shockbytes.plugin.service.apps.AppsSyncService
import at.shockbytes.plugin.util.ConfigManager
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.border.EmptyBorder

class AndroidWorker : Worker(), ActionListener {

    private lateinit var outputArea: JTextArea
    private lateinit var textFieldIp: JTextField
    private lateinit var textFieldPort: JTextField
    private lateinit var textFieldPortWear: JTextField
    private lateinit var btnDiscoverIp: JButton
    private lateinit var btnConnect: JButton
    private lateinit var btnConnectWear: JButton
    private lateinit var btnDisconnect: JButton
    private lateinit var btnRestartServer: JButton
    private lateinit var btnSendToDevice: JButton
    private lateinit var btnDebugCert: JButton
    private lateinit var btnShockCert: JButton
    private lateinit var labelInfo: JLabel

    private val adbService: AdbService = WindowsAdbService()
    private val appsSyncer: AppsSyncService = AppsSyncService(ProjectManager.getInstance().openProjects[0])
    private val certificateService: CertificateService = KeyStoreBackedCertificateService()

    override val title = "Android Utilities"
    override val icon = IconLoader.getIcon("/icons/tab_android.png")

    override fun initializePanel() {
        rootPanel = JPanel(BorderLayout())

        outputArea = JTextArea()
        rootPanel.add(JBScrollPane(outputArea), BorderLayout.CENTER)

        val configPanelRight = JPanel()
        configPanelRight.border = EmptyBorder(4, 4, 4, 4)
        configPanelRight.layout = GridLayout(4, 2, 12, 12)

        configPanelRight.add(JLabel("Port"))
        textFieldPort = JTextField("5560", 10)
        configPanelRight.add(textFieldPort)
        configPanelRight.add(JLabel("Ip address"))
        textFieldIp = JTextField("", 10)
        configPanelRight.add(textFieldIp)
        configPanelRight.add(JLabel(""))
        configPanelRight.add(JLabel(""))
        configPanelRight.add(JLabel("Android wear port"))
        textFieldPortWear = JTextField("4446", 10)
        configPanelRight.add(textFieldPortWear)

        val configPanelLeft = JPanel()
        configPanelLeft.layout = GridLayout(4, 2, 4, 4)
        btnConnect = JButton("Connect", IconLoader.getIcon("/icons/ic_connect.png"))
        configPanelLeft.add(btnConnect)
        btnConnectWear = JButton("Connect wear", IconLoader.getIcon("/icons/tab_wear.png"))
        configPanelLeft.add(btnConnectWear)
        btnDisconnect = JButton("Disconnect", IconLoader.getIcon("/icons/ic_disconnect.png"))
        configPanelLeft.add(btnDisconnect)
        btnDiscoverIp = JButton("Discover device", IconLoader.getIcon("/icons/ic_discover.png"))
        configPanelLeft.add(btnDiscoverIp)
        btnRestartServer = JButton("Restart adb", IconLoader.getIcon("/icons/ic_restart.png"))
        configPanelLeft.add(btnRestartServer)
        btnSendToDevice = JButton("APK 2 device", IconLoader.getIcon("/icons/ic_send_push.png"))
        configPanelLeft.add(btnSendToDevice)

        btnDebugCert = JButton("Debug certificate", IconLoader.getIcon("/icons/ic_debug_certificate.png"))
        configPanelLeft.add(btnDebugCert)

        btnShockCert = JButton("Shockbytes certificate", IconLoader.getIcon("/icons/ic_shockbytes_cert.png"))
        configPanelLeft.add(btnShockCert)

        btnConnect.addActionListener(this)
        btnDiscoverIp.addActionListener(this)
        btnDisconnect.addActionListener(this)
        btnConnectWear.addActionListener(this)
        btnRestartServer.addActionListener(this)
        btnSendToDevice.addActionListener(this)
        btnDebugCert.addActionListener(this)
        btnShockCert.addActionListener(this)

        labelInfo = JLabel("")

        rootPanel.add(configPanelRight, BorderLayout.EAST)
        rootPanel.add(configPanelLeft, BorderLayout.WEST)
        rootPanel.add(labelInfo, BorderLayout.SOUTH)
    }

    override fun actionPerformed(e: ActionEvent) {
        when {
            e.source === btnDiscoverIp -> discoverDeviceIp()
            e.source === btnConnect -> connect()
            e.source === btnDisconnect -> disconnect()
            e.source === btnConnectWear -> connectWearDevice()
            e.source === btnRestartServer -> restartAdbServer()
            e.source === btnSendToDevice -> sendApkToDevice()
            e.source === btnDebugCert -> showDebugCertificateInformation()
            e.source === btnShockCert -> showCustomCertificateInformation()
        }
    }

    private fun discoverDeviceIp() {
        adbService.discoverDeviceIp().subscribe { (msg, deviceIp) ->
            outputArea.append("\n$msg\n")
            if (deviceIp != null) {
                textFieldIp.text = deviceIp
            }
        }
    }

    private fun connect() {

        val port = textFieldPort.text.toInt()
        val deviceIp = textFieldIp.text

        adbService.connectToDevice(deviceIp, port).subscribe({ (processOutput, connectedDevice) ->
            outputArea.append(processOutput)
            outputArea.append("$connectedDevice\n")
            updateConnectionLabel(connectedDevice)
        }, { outputArea.append("Error while connecting...\n") })
    }

    private fun connectWearDevice() {
        textFieldPortWear.text.toIntOrNull()?.let { port ->
            adbService.connectToWearable(port).subscribe { output ->
                outputArea.append(output)
            }
        }
    }

    private fun disconnect() {
        adbService.disconnect().subscribe { output ->
            outputArea.append(output)
        }
    }

    private fun restartAdbServer() {
        adbService.restartAdbServer().subscribe { output ->
            outputArea.append(output)
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

            labelInfo.text = "Connected device(s): $info"
        }
    }

    private fun sendApkToDevice() {
        appsSyncer.tryCopyDebugAPK(outputArea).subscribe({
            // Do nothing here...
        }, { throwable ->
            outputArea.append("Error while sending apk to device (" + throwable.message + ")...\n")
        })
    }

    private fun showDebugCertificateInformation() {
        certificateService.getDebugCertificate(ConfigManager.loadDebugCertificatePath())
                .subscribe { certInfo ->
                    outputArea.append(certInfo)
                }
    }

    private fun showCustomCertificateInformation() {
        certificateService.getCustomCertificate(ConfigManager.loadCustomCertificate())
                .subscribe { certInfo ->
                    outputArea.append(certInfo)
                }
    }

}
