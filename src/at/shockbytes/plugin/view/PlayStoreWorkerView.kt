package at.shockbytes.plugin.view

import at.shockbytes.plugin.worker.PlayStoreWorker
import io.reactivex.disposables.CompositeDisposable
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

class PlayStoreWorkerView(private val playStoreWorker: PlayStoreWorker): WorkerView<JPanel>() {

    override fun initializeView(): JPanel {
        val rootPanel = JPanel(BorderLayout())
        rootPanel.add(JLabel("Use Fastlane/Supply for faster publishing - Coming in Version 3.2"), BorderLayout.CENTER)

        return rootPanel
    }

    override fun observeWorker(): CompositeDisposable? {
        return null
    }
}