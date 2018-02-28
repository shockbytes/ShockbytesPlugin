package at.shockbytes.plugin.worker

import at.shockbytes.plugin.util.HelperUtil
import at.shockbytes.plugin.util.ConfigManager
import at.shockbytes.plugin.view.MaterialListCellRenderer
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.GridLayout
import java.awt.Point
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * Author:  Mescht
 * Date:    09.01.2017
 */
class WorkspaceCrawlerWorker : Worker(), ActionListener {

    private lateinit var textFieldInput: JTextField
    private lateinit var cbExcludeBinaries: JCheckBox
    private lateinit var cbExcludeProjectFiles: JCheckBox
    private lateinit var cbExcludeGeneratedFiles: JCheckBox
    private lateinit var labelStatus: JLabel
    private lateinit var searchList: JBList<String>

    private val workspaceDirectory = ConfigManager.loadWorkspaceLocation()

    override val title = "Workspace Crawler"
    override val icon = IconLoader.getIcon("/icons/tab_workspace_crawler.png")

    override fun initializePanel() {

        rootPanel = JPanel(BorderLayout())

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
        searchList.cellRenderer = MaterialListCellRenderer()
        searchList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {

                var file = searchList.selectedValue
                if (e?.clickCount == 2) {
                    openFile(File(file))
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    val idxSel = searchList.locationToIndex(e?.point)
                    searchList.selectedIndex = idxSel
                    file = searchList.selectedValue.toString()
                    showPopup(e, file)
                }
            }
        })
        rootPanel.add(JBScrollPane(searchList), BorderLayout.CENTER)
        initialize()
    }

    private fun initialize() {
        searchList.setEmptyText("Ready")
    }

    private fun showPopup(e: MouseEvent?, filename: String) {

        val menu = JPopupMenu()
        val itemOpen = JMenuItem("Open folder in explorer")
        itemOpen.icon = IconLoader.getIcon("/icons/ic_open_in_explorer.png")
        itemOpen.addActionListener { openFile(File(filename).parentFile) }
        menu.add(itemOpen)
        val itemCopy = JMenuItem("Copy to workspace")
        itemCopy.icon = IconLoader.getIcon("/icons/ic_copy.png")
        itemCopy.addActionListener { copyIntoWorkspace(File(filename), e?.locationOnScreen ?: Point(0, 0)) }
        menu.add(itemCopy)
        menu.show(searchList, e?.point?.x ?: 0, e?.point?.y ?: 0)
    }

    private fun openFile(f: File) {
        try {
            Desktop.getDesktop().open(f)
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

    }

    override fun actionPerformed(e: ActionEvent) {

        labelStatus.text = "Searching..."
        searchList.setEmptyText("Searching...")
        searchList.setPaintBusy(true)
        Thread { searchForKeywordNative(textFieldInput.text) }.start()
    }

    private fun searchForKeywordNative(keyword: String) {

        try {

            val start = System.currentTimeMillis()

            val files = File(workspaceDirectory).walk().maxDepth(15)
                    .filter { fileFilter(it, keyword) }
                    .map { it.absolutePath }
                    .toList().toTypedArray()

            val duration = System.currentTimeMillis() - start

            SwingUtilities.invokeLater {
                searchList.setListData(files)
                labelStatus.text = files.size.toString() + " files found (in " + duration / 1000 + "s)"

                if (files.isEmpty()) {
                    searchList.setEmptyText("Nothing found for <$keyword>")
                }
                searchList.setPaintBusy(false)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun copyIntoWorkspace(file: File, p: Point) {

        val projects = ProjectManager.getInstance().openProjects
        if (projects.isEmpty()) {
            JOptionPane.showMessageDialog(rootPanel, "There is no open project!")
            return
        }
        showCopyDialog(file, p, HelperUtil.getPackagesFromProject(projects[0]),
                HelperUtil.getSourceRootFolder(projects[0]))
    }

    private fun showCopyDialog(f: File, p: Point, packages: List<String>, sourceRootPath: String?) {

        println(f.absolutePath)
        val dialog = JDialog()
        dialog.title = "Copy file into project"
        dialog.layout = GridLayout(5, 1)
        val width = 200
        dialog.setBounds(p.x, p.y - width, width, width)
        dialog.isModal = true
        dialog.add(JLabel("Choose destination package"))
        val comboBox = ComboBox(CollectionComboBoxModel<String>(HelperUtil.getDisplayablePackageName(packages)))
        dialog.add(comboBox)

        val textField = JTextField(f.name)
        dialog.add(textField)
        dialog.add(JLabel())
        val btnCopy = JButton("Copy", IconLoader.getIcon("/icons/ic_copy.png"))
        btnCopy.addActionListener {

            val pkg = packages[comboBox.selectedIndex]
            val destination = sourceRootPath + pkg + "/" + textField.text
            val src = f.absolutePath

            try {
                Files.copy(Paths.get(src), Paths.get(destination), StandardCopyOption.COPY_ATTRIBUTES)
            } catch (e1: IOException) {
                e1.printStackTrace()
                JOptionPane.showMessageDialog(dialog, e1.message, "Copy error", JOptionPane.ERROR_MESSAGE)
            }

            dialog.dispose()
        }
        dialog.add(btnCopy)
        dialog.isVisible = true
    }

    private fun fileFilter(f: File, keyword: String): Boolean {

        val n = f.name
        return when {

            // Check if keyword is in path and it is not a directory
            (!n.contains(keyword) || Files.isDirectory(f.toPath())) -> false

            // Check for binary files
            cbExcludeBinaries.isSelected && (n.endsWith(".class") || n.endsWith(".exe")
                    || n.endsWith(".jar") || n.endsWith(".bat") || n.endsWith(".dex")) -> false

            // Check for project files
            cbExcludeProjectFiles.isSelected && (n.endsWith(".dependency") || n.endsWith(".xml")
                    || n.endsWith(".iml") || n.endsWith(".properties")) -> false

            // Check for generated files
            cbExcludeGeneratedFiles.isSelected && (n.contains("$\$ViewBinder")
                    || f.absolutePath.contains("build\\generated\\source\\kapt")
                    || f.absolutePath.contains("build\\generated\\source\\apt")
                    || f.absolutePath.contains("build\\tmp\\kapt3\\stubs")) -> false

            else -> true
        }
    }
}
