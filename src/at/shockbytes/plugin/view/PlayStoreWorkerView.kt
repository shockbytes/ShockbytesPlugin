package at.shockbytes.plugin.view

import at.shockbytes.plugin.util.UiUtils
import at.shockbytes.plugin.util.addTo
import at.shockbytes.plugin.worker.PlayStoreWorker
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBScrollPane
import io.reactivex.disposables.CompositeDisposable
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.border.EmptyBorder

class PlayStoreWorkerView(private val playStoreWorker: PlayStoreWorker) : WorkerView<JPanel>(), ActionListener {

    private lateinit var btnAddFastlane: JButton
    private lateinit var btnConfigBeta: JButton
    private lateinit var btnConfigRelease: JButton
    private lateinit var btnPublishBeta: JButton
    private lateinit var btnPublishRelease: JButton
    private lateinit var btnUpdateNotes: JButton
    private lateinit var btnScreenshots: JButton
    private lateinit var btnVersioning: JButton
    private lateinit var btnPrepareForRelease: JButton

    private lateinit var textArea: JTextArea

    override fun initializeView(): JPanel {

        val rootPanel = JPanel(BorderLayout())

        val buttonPanel = JPanel(GridLayout(4, 1))
        btnAddFastlane = JButton("Initialize Fastlane")
        btnAddFastlane.addActionListener(this)
        buttonPanel.add(btnAddFastlane)

        btnConfigBeta = JButton("Configure beta lane")
        btnConfigBeta.addActionListener(this)
        buttonPanel.add(btnConfigBeta)

        btnConfigRelease = JButton("Configure release lane")
        btnConfigRelease.addActionListener(this)
        buttonPanel.add(btnConfigRelease)

        rootPanel.add(buttonPanel, BorderLayout.WEST)

        val releasePanel = JPanel(FlowLayout())
        btnPublishBeta = JButton("Publish Beta", IconLoader.getIcon("/icons/ic_beta.png"))
        btnPublishBeta.addActionListener(this)
        releasePanel.add(btnPublishBeta)

        btnPublishRelease = JButton("Publish Release", IconLoader.getIcon("/icons/tab_google_play.png"))
        btnPublishRelease.addActionListener(this)
        releasePanel.add(btnPublishRelease)

        rootPanel.add(releasePanel, BorderLayout.SOUTH)

        val releaseNotesPanel = JPanel(GridLayout(4, 1))
        btnUpdateNotes = JButton("Manage release notes", IconLoader.getIcon("/icons/ic_update_notes.png"))
        btnUpdateNotes.addActionListener(this)
        releaseNotesPanel.add(btnUpdateNotes)

        btnScreenshots = JButton("Manage screenshots", IconLoader.getIcon("/icons/ic_screenshots.png"))
        btnScreenshots.addActionListener(this)
        releaseNotesPanel.add(btnScreenshots)

        btnVersioning = JButton("Manage versions", IconLoader.getIcon("/icons/ic_versions.png"))
        btnVersioning.addActionListener(this)
        releaseNotesPanel.add(btnVersioning)

        btnPrepareForRelease = JButton("Prepare for release", IconLoader.getIcon("/icons/ic_prepare.png"))
        btnPrepareForRelease.addActionListener(this)
        releaseNotesPanel.add(btnPrepareForRelease)


        rootPanel.add(releaseNotesPanel, BorderLayout.EAST)

        textArea = JTextArea()
        textArea.border = EmptyBorder(8, 8, 8, 8)
        rootPanel.add(JBScrollPane(textArea), BorderLayout.CENTER)

        return rootPanel
    }

    override fun observeWorker(): CompositeDisposable? {
        val disposables = CompositeDisposable()

        playStoreWorker.fastLaneInitializedEvent.subscribe {
            setProjectUiInitialized(it)
        }.addTo(disposables)

        playStoreWorker.textOutputEvent.subscribe {
            textArea.append("$it${System.lineSeparator()}")
        }.addTo(disposables)

        return disposables
    }

    private fun setProjectUiInitialized(isInitialized: Boolean) {

        btnAddFastlane.isEnabled = !isInitialized

        btnPublishRelease.isEnabled = isInitialized
        btnConfigBeta.isEnabled = isInitialized
        btnConfigRelease.isEnabled = isInitialized
        btnPublishBeta.isEnabled = isInitialized
        btnScreenshots.isEnabled = isInitialized
        btnUpdateNotes.isEnabled = isInitialized
        btnVersioning.isEnabled = isInitialized
    }

    override fun actionPerformed(e: ActionEvent?) {

        when (e?.source) {

            btnAddFastlane -> {

                val jsonKeyFile = UiUtils.showJsonOpenDialog("Open json secret for PlayStore console access")
                val packageName = JOptionPane.showInputDialog("Please enter the package name.")

                if (jsonKeyFile != null) {
                    playStoreWorker.initializeFastlane(jsonKeyFile, packageName)
                } else {
                    JOptionPane.showMessageDialog(null, "Json key file must point to a valid direction!")
                }
            }
            btnConfigBeta -> playStoreWorker.configureBetaLane()
            btnConfigRelease -> playStoreWorker.configureReleaseLane()
            btnPublishBeta -> playStoreWorker.publishBetaRelease()
            btnPublishRelease -> playStoreWorker.publishRelease()
            btnScreenshots -> playStoreWorker.manageScreenshots()
            btnUpdateNotes -> playStoreWorker.manageReleaseNotes()
            btnVersioning -> playStoreWorker.manageVersions()
            btnPrepareForRelease -> playStoreWorker.prepareForRelease()
        }

    }

}