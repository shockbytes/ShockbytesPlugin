package at.shockbytes.plugin.view

import at.shockbytes.plugin.service.workspace.CrawlOptions
import at.shockbytes.plugin.ui.MaterialListCellRenderer
import at.shockbytes.plugin.util.addTo
import at.shockbytes.plugin.worker.WorkspaceCrawlerWorker
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import io.reactivex.disposables.CompositeDisposable
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder

class WorkspaceCrawlerWorkerView(private val workspaceCrawlerWorker: WorkspaceCrawlerWorker): WorkerView<JPanel>(), ActionListener {

    private lateinit var textFieldInput: JTextField
    private lateinit var cbExcludeBinaries: JCheckBox
    private lateinit var cbExcludeProjectFiles: JCheckBox
    private lateinit var cbExcludeGeneratedFiles: JCheckBox
    private lateinit var labelStatus: JLabel
    private lateinit var searchList: JBList<String>

    override fun initializeView(): JPanel {
        val rootPanel = JPanel(BorderLayout())

        val searchPanel = JPanel(GridLayout(5, 1, 2, 2))
        searchPanel.border = EmptyBorder(8, 8, 8, 8)

        textFieldInput = JTextField("Search", 15)
        textFieldInput.addActionListener(this)
        searchPanel.add(textFieldInput)
        cbExcludeBinaries = JCheckBox("Exclude Binaries", true)
        searchPanel.add(cbExcludeBinaries)
        cbExcludeProjectFiles = JCheckBox("Exclude project files", true)
        searchPanel.add(cbExcludeProjectFiles)
        cbExcludeGeneratedFiles = JCheckBox("Exclude generated files", true)
        searchPanel.add(cbExcludeGeneratedFiles)
        labelStatus = JLabel("Ready")
        searchPanel.add(labelStatus)

        rootPanel.add(searchPanel, BorderLayout.WEST)

        searchList = JBList()
        searchList.setEmptyText("Ready")
        searchList.cellRenderer = MaterialListCellRenderer()
        searchList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {

                var file = searchList.selectedValue
                if (e?.clickCount == 2) {
                    workspaceCrawlerWorker.openFile(File(file))
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    val idxSel = searchList.locationToIndex(e?.point)
                    searchList.selectedIndex = idxSel
                    file = searchList.selectedValue.toString()
                    workspaceCrawlerWorker.showPopup(searchList, e, file)
                }
            }
        })
        rootPanel.add(JBScrollPane(searchList), BorderLayout.CENTER)

        return rootPanel
    }

    override fun observeWorker(): CompositeDisposable? {
        val disposables = CompositeDisposable()

        workspaceCrawlerWorker.crawlObservable.subscribe { (files, keyword, duration) ->

            SwingUtilities.invokeLater {
                searchList.setListData(files)
                labelStatus.text = "${files.size} files found (in ${duration}s)"

                if (files.isEmpty()) {
                    searchList.setEmptyText("Nothing found for <$keyword>")
                }
                searchList.setPaintBusy(false)
            }
        }.addTo(disposables)

        workspaceCrawlerWorker.crawlErrorObservable.subscribe { throwable ->
            searchList.setEmptyText("Error while crawling the workspace: ${throwable.localizedMessage}")
        }.addTo(disposables)

        return disposables
    }


    override fun actionPerformed(e: ActionEvent) {

        labelStatus.text = "Searching..."
        searchList.setEmptyText("Searching...")
        searchList.setPaintBusy(true)

        workspaceCrawlerWorker.crawl(textFieldInput.text, CrawlOptions(cbExcludeBinaries.isSelected,
                cbExcludeProjectFiles.isSelected, cbExcludeGeneratedFiles.isSelected))

    }

}