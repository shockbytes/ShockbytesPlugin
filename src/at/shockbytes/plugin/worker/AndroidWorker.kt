package at.shockbytes.plugin.worker

import at.shockbytes.plugin.model.CertificateParams
import at.shockbytes.plugin.service.apps.AppsSyncService
import at.shockbytes.plugin.util.HelperUtil
import at.shockbytes.plugin.util.ShockConfig
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.security.KeyStore
import java.security.MessageDigest
import java.security.cert.Certificate
import javax.swing.*
import javax.swing.border.EmptyBorder

class AndroidWorker : Worker(), ActionListener {

    private val separator = "----------------------------------------------------------------------\n"

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

    private var appsSyncer: AppsSyncService? = null

    override val title = "Android Utilities"
    override val icon = IconLoader.getIcon("/icons/tab_android.png")

    override fun initializePanel() {

        rootPanel = JPanel()
        rootPanel.layout = BorderLayout()

        outputArea = JTextArea()
        rootPanel.add(JBScrollPane(outputArea), BorderLayout.CENTER)

        val configPanelRight = JPanel()
        configPanelRight.border = EmptyBorder(4, 4, 4, 4)
        configPanelRight.layout = GridLayout(4, 2, 12, 12)

        configPanelRight.add(JLabel("Port"))
        textFieldPort = JTextField("5560", 10)
        configPanelRight.add(textFieldPort)
        configPanelRight.add(JLabel("Ip adress"))
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
        btnRestartServer = JButton("Restart ADB", IconLoader.getIcon("/icons/ic_restart.png"))
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

        initializeObjects()
    }

    @Throws(IOException::class)
    private fun discoverDeviceIp() {

        val cmdShowIp = "adb shell ip -f inet addr show wlan0"
        val p = Runtime.getRuntime().exec(cmdShowIp)
        val inStream = BufferedReader(InputStreamReader(p.inputStream))
        val ipPrefix = "inet"

        inStream.useLines { lines ->
            lines.forEach {
                val line = it.trim { it <= ' ' }
                if (line.startsWith(ipPrefix)) {
                    val idxStart = line.indexOf(" ") + 1
                    val idxEnd = line.indexOf("/")

                    val deviceIp = line.substring(idxStart, idxEnd)
                    outputArea.append("\nDevice ip found <$deviceIp>\n")
                    textFieldIp.text = deviceIp
                    return
                }
            }
        }

        outputArea.append("Cannot retrieve ip of device...\n")
    }

    private fun initializeObjects() {
        appsSyncer = AppsSyncService(ProjectManager.getInstance().openProjects[0])
    }

    @Throws(IOException::class)
    private fun connect() {

        val port = Integer.parseInt(textFieldPort.text)
        val deviceIp = textFieldIp.text

        val cmdTcpIp = "adb tcpip " + port
        val cmdConnect = "adb connect $deviceIp:$port"
        val cmdDevices = "adb devices -l"

        Runtime.getRuntime().exec(cmdTcpIp)
        val connectProcess = Runtime.getRuntime().exec(cmdConnect)
        outputArea.append(HelperUtil.getOutputFromProcess(connectProcess) + "\n")

        val deviceProcess = Runtime.getRuntime().exec(cmdDevices)
        val connectedDevice = HelperUtil.getOutputFromProcess(deviceProcess)
        outputArea.append(connectedDevice)

        updateConnectionLabel(connectedDevice)
    }

    @Throws(IOException::class)
    private fun connectWearDevice() {

        val portStr = textFieldPortWear.text
        if (portStr.isNullOrEmpty()) {
            outputArea.append("Android Wear connection port value must not be empty!\n")
            return
        }

        val port = Integer.parseInt(portStr)
        val cmdForwardTcp = "adb forward tcp:$port localabstract:/adb-hub"
        val cmdConnect = "adb connect 127.0.0.1:" + port

        Runtime.getRuntime().exec(cmdForwardTcp)
        val p = Runtime.getRuntime().exec(cmdConnect)
        val output = HelperUtil.getOutputFromProcess(p)
        outputArea.append(output + "\n\n")
    }

    @Throws(IOException::class)
    private fun disconnect() {

        val cmdDisconnect = "adb usb"
        outputArea.append("Switch back to USB connection.\n")

        val disconnectProcess = Runtime.getRuntime().exec(cmdDisconnect)
        outputArea.append(HelperUtil.getOutputFromProcess(disconnectProcess) + "\n\n")
    }

    @Throws(IOException::class)
    private fun restartAdbServer() {

        val cmdKillServer = "adb kill-server"
        val cmdStartServer = "adb start-server"

        Runtime.getRuntime().exec(cmdKillServer)
        outputArea.append("ADB server killed...\n")
        val p = Runtime.getRuntime().exec(cmdStartServer)
        val output = HelperUtil.getOutputFromProcess(p)
        outputArea.append(output + "\n\n")
    }

    private fun updateConnectionLabel(connectedDevice: String?) {

        if (connectedDevice.isNullOrEmpty()) {
            return
        }

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
        } while(startIdx > -1)

        labelInfo.text = "Connected device(s): " + info
    }

    private fun sendApkToDevice() {

        try {
            appsSyncer?.tryCopyDebugAPK(outputArea)
        } catch (e: IOException) {
            e.printStackTrace()
            outputArea.append("Error while sending apk to device (" + e.message + ")...\n")
        }

    }

    private fun showDebugCertificateInformation() {

        val keyStorePath = System.getProperty("user.home") + "\\.android\\debug.keystore"
        val certInfo: String = try {
            grabCertificateInformation(keyStorePath, "androiddebugkey",
                    "android".toCharArray(), "android".toCharArray(), true)
        } catch (e: Exception) {
            e.printStackTrace()
            "Cannot access debug KeyStore located at: " + keyStorePath + "\n" + separator
        }

        outputArea.append(certInfo)
    }

    private fun showCustomCertificateInformation() {

        var certParams: CertificateParams? = null
        val certInfo = try {
            certParams = ShockConfig.loadCustomCertificate()
            grabCertificateInformation(certParams.keyStorePath, certParams.alias,
                    certParams.keyStorePassword, certParams.entryPassword, false)
        } catch (e: Exception) {
            e.printStackTrace()
            "Cannot access custom KeyStore located at: " + certParams?.keyStorePath + "\n" + separator
        }

        outputArea.append(certInfo)
    }

    @Throws(Exception::class)
    private fun grabCertificateInformation(keyStorePath: String?, alias: String, keyStorePassword: CharArray,
                                           entryPassword: CharArray, isDebug: Boolean): String {

        val inStream = FileInputStream(keyStorePath!!)
        val keystore = KeyStore.getInstance(KeyStore.getDefaultType())
        keystore.load(inStream, keyStorePassword)

        val entry = keystore.getEntry(alias,
                KeyStore.PasswordProtection(entryPassword)) as KeyStore.PrivateKeyEntry

        val header = if (isDebug)
            "DEBUG CERTIFICATE FINGERPRINTS\nDebug certificate located at: "
        else
            "CUSTOM CERTIFICATE FINGERPRINTS\nCustom certificate located at: "
        var info = header + keyStorePath + "\n\n"
        info += "SHA1:\t" + getCertFingerPrint("SHA1", entry.certificate) + "\n"
        info += "MD5:\t" + getCertFingerPrint("MD5", entry.certificate) + "\n"
        info += separator
        return info
    }

    @Throws(Exception::class)
    private fun getCertFingerPrint(mdAlg: String, cert: Certificate): String {
        val encCertInfo = cert.encoded
        val md = MessageDigest.getInstance(mdAlg)
        val digest = md.digest(encCertInfo)
        return HelperUtil.toHexString(digest)
    }


    override fun actionPerformed(e: ActionEvent) {

        when {
            e.source === btnDiscoverIp -> try {
                discoverDeviceIp()
            } catch (e1: IOException) {
                e1.printStackTrace()
                outputArea.append("Error while retrieving ip address (" + e1.message + ")...\n")
            }
            e.source === btnConnect -> try {
                connect()
            } catch (e1: IOException) {
                e1.printStackTrace()
                outputArea.append("Error while connecting (" + e1.message + ")...\n")
            }
            e.source === btnDisconnect -> try {
                disconnect()
            } catch (e1: IOException) {
                e1.printStackTrace()
                outputArea.append("Error while disconnecting (" + e1.message + ")...\n")
            }
            e.source === btnConnectWear -> try {
                connectWearDevice()
            } catch (e1: IOException) {
                e1.printStackTrace()
                outputArea.append("Error while connecting wear device (" + e1.message + ")...\n")
            }
            e.source === btnRestartServer -> try {
                restartAdbServer()
            } catch (e1: IOException) {
                e1.printStackTrace()
                outputArea.append("Error while restarting server (" + e1.message + ")...\n")
            }
            e.source === btnSendToDevice -> sendApkToDevice()
            e.source === btnDebugCert -> showDebugCertificateInformation()
            e.source === btnShockCert -> showCustomCertificateInformation()
        }
    }
}
