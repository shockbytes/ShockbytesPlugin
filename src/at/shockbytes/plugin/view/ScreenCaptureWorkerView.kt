package at.shockbytes.plugin.view

import at.shockbytes.plugin.util.addTo
import at.shockbytes.plugin.worker.ScreenCaptureWorker
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBScrollPane
import io.reactivex.disposables.CompositeDisposable
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class ScreenCaptureWorkerView(private val screenCaptureWorker: ScreenCaptureWorker) : WorkerView<JPanel>(), ActionListener {

    private lateinit var btnStartAdb: JButton
    private lateinit var btnStopAdb: JButton
    private lateinit var btnPlayAdb: JButton
    private lateinit var textAreaAdb: JTextArea

    override fun initializeView(): JPanel {

        val rootPanel = JPanel(BorderLayout())

        val panelScreengrab = JPanel(BorderLayout())
        panelScreengrab.add(JLabel("Screengrab coming in version 4.1"))

        val panelAdb = JPanel(BorderLayout())
        textAreaAdb = JTextArea()
        textAreaAdb.isEditable = false
        panelAdb.add(JBScrollPane(textAreaAdb))

        val controlAdbPanel = JPanel(GridLayout(4, 1, 2, 2))
        btnStartAdb = JButton("Start", IconLoader.getIcon("/icons/ic_start.png"))
        btnStartAdb.addActionListener(this)
        controlAdbPanel.add(btnStartAdb)
        btnStopAdb = JButton("Stop", IconLoader.getIcon("/icons/ic_stop.png"))
        btnStopAdb.addActionListener(this)
        btnStopAdb.isEnabled = false
        controlAdbPanel.add(btnStopAdb)
        btnPlayAdb = JButton("Play", IconLoader.getIcon("/icons/ic_play.png"))
        btnPlayAdb.addActionListener(this)
        btnPlayAdb.isEnabled = false
        controlAdbPanel.add(btnPlayAdb)
        panelAdb.add(controlAdbPanel, BorderLayout.EAST)

        val mainPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, panelScreengrab, panelAdb)
        mainPane.setDividerLocation(0.5)
        mainPane.resizeWeight = 0.5
        rootPanel.add(mainPane)

        return rootPanel
    }

    override fun observeWorker(): CompositeDisposable? {
        val disposable = CompositeDisposable()

        screenCaptureWorker.buttonStateObservable.subscribe { (isCapturing, onError) ->

            SwingUtilities.invokeLater {
                // start button was clicked
                if (isCapturing && !onError) {
                    btnStopAdb.isEnabled = true
                    btnPlayAdb.isEnabled = false
                    btnStartAdb.isEnabled = false
                } else if (!isCapturing && onError) {   // screen capturing throws exception
                    btnStopAdb.isEnabled = false
                    btnPlayAdb.isEnabled = false
                } else if (!isCapturing) {  // stop capturing was clicked
                    btnStopAdb.isEnabled = false
                    btnPlayAdb.isEnabled = true
                    btnStartAdb.isEnabled = true
                }
            }
        }.addTo(disposable)

        screenCaptureWorker.textObservable.subscribe { text ->
            SwingUtilities.invokeLater { textAreaAdb.append(text) }
        }.addTo(disposable)

        screenCaptureWorker.buttonPlayStateObservable.subscribe { isEnabled ->
            btnPlayAdb.isEnabled = isEnabled
        }.addTo(disposable)

        return disposable
    }

    override fun actionPerformed(e: ActionEvent) {
        when {
            e.source === btnStartAdb -> screenCaptureWorker.startScreenCapturing()
            e.source === btnStopAdb -> {
                val filePath = showFileStorageDialog() ?: return
                screenCaptureWorker.stopScreenCapturing(filePath)
            }
            e.source === btnPlayAdb -> screenCaptureWorker.playRecordedVideo()
        }
    }

    private fun showFileStorageDialog(): String? {

        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Select file to store screen recording"
        fileChooser.isMultiSelectionEnabled = false
        fileChooser.selectedFile = File("screenrecord.mp4")
        fileChooser.fileFilter = FileNameExtensionFilter("mp4 files", "mp4")
        fileChooser.dragEnabled = true
        val approval = fileChooser.showOpenDialog(textAreaAdb)
        return if (approval == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFile.absolutePath
        } else null
    }

}