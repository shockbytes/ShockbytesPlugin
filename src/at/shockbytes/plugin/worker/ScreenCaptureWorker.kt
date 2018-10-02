package at.shockbytes.plugin.worker

import at.shockbytes.plugin.service.android.AdbService
import at.shockbytes.plugin.service.android.WindowsAdbService
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Author:  Martin Macheiner
 * Date:    31.01.2017
 */
class ScreenCaptureWorker(private val adbService: AdbService) : Worker(), ActionListener {

    private lateinit var btnStartAdb: JButton
    private lateinit var btnStopAdb: JButton
    private lateinit var btnPlayAdb: JButton
    private lateinit var textAreaAdb: JTextArea

    private var recordedPath: String? = null

    override val title = "Screen Capture"
    override val icon = IconLoader.getIcon("/icons/tab_screen_record.png")

    override fun initializePanel() {

        rootPanel = JPanel(BorderLayout())

        val panelScreengrab = JPanel(BorderLayout())
        panelScreengrab.add(JLabel("Screengrab coming in version 3.1"))

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
    }

    override fun actionPerformed(e: ActionEvent) {
        when {
            e.source === btnStartAdb -> startScreenCapturing()
            e.source === btnStopAdb -> stopScreenCapturing()
            e.source === btnPlayAdb -> playRecordedVideo()
        }
    }

    private fun updateButtons(isCapturing: Boolean, onError: Boolean) {

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
    }

    private fun updateTextArea(text: String) {
        SwingUtilities.invokeLater { textAreaAdb.append(text) }
    }

    private fun startScreenCapturing() {
        updateButtons(true, false)

        adbService.startScreenCapturing().subscribe({ output ->
            updateTextArea(output)
        }, { throwable ->
            throwable.printStackTrace()
            updateButtons(false, true)
            updateTextArea("${throwable.localizedMessage}\n")
        })
    }

    private fun showFileStorageDialog(): String? {

        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Select file to store screen recording"
        fileChooser.isMultiSelectionEnabled = false
        fileChooser.selectedFile = File("screenrecord.mp4")
        fileChooser.fileFilter = FileNameExtensionFilter("mp4 files", "mp4")
        fileChooser.dragEnabled = true
        val approval = fileChooser.showOpenDialog(rootPanel)
        return if (approval == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFile.absolutePath
        } else null
    }

    private fun stopScreenCapturing() {

        val filePath = showFileStorageDialog() ?: return
        adbService.stopScreenCapturing(filePath).subscribe({ (output, destination) ->
            updateButtons(false, false)
            updateTextArea(output)
            recordedPath = destination
        }, { throwable ->
            throwable.printStackTrace()
            btnPlayAdb.isEnabled = false
            updateTextArea("${throwable.message}\n")
        })
    }

    private fun playRecordedVideo() {

        try {
            Desktop.getDesktop().open(File(recordedPath))
            updateTextArea("Play file: $recordedPath\n")
        } catch (e: Exception) {
            e.printStackTrace()
            updateTextArea("${e.localizedMessage}\n")
        }
    }

}

