package at.shockbytes.plugin.view

import at.shockbytes.plugin.util.addTo
import at.shockbytes.plugin.worker.AndroidWorker
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBScrollPane
import io.reactivex.disposables.CompositeDisposable
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.border.EmptyBorder

class AndroidWorkerView(private val androidWorker: AndroidWorker) : WorkerView<JPanel>(), ActionListener {

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

    override fun initializeView(): JPanel {
        val rootPanel = JPanel(BorderLayout())

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

        return rootPanel
    }

    override fun observeWorker(): CompositeDisposable? {
        val disposables = CompositeDisposable()

        androidWorker.outputObservable.subscribe { data -> outputArea.append(data) }.addTo(disposables)
        androidWorker.infoObservable.subscribe { info -> labelInfo.text = info }.addTo(disposables)
        androidWorker.ipObservable.subscribe { ip -> textFieldIp.text = ip }.addTo(disposables)

        return disposables
    }

    override fun actionPerformed(e: ActionEvent) {
        when {
            e.source === btnDiscoverIp -> androidWorker.discoverDeviceIp()
            e.source === btnConnect -> androidWorker.connect(textFieldIp.text, textFieldPort.text.toInt())
            e.source === btnDisconnect -> androidWorker.disconnect()
            e.source === btnConnectWear -> androidWorker.connectWearDevice(textFieldPortWear.text.toIntOrNull())
            e.source === btnRestartServer -> androidWorker.restartAdbServer()
            e.source === btnSendToDevice -> androidWorker.sendApkToDevice()
            e.source === btnDebugCert -> androidWorker.showDebugCertificateInformation()
            e.source === btnShockCert -> androidWorker.showCustomCertificateInformation()
        }
    }

}