package at.shockbytes.plugin.worker

import at.shockbytes.plugin.util.GradleDependencyInjector
import at.shockbytes.plugin.util.IdeaProjectUtils
import at.shockbytes.plugin.ui.MaterialListCellRenderer
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.TitledBorder

/**
 * Author:  Martin Macheiner
 * Date:    21.02.2017
 */
class GradleWorker : Worker(), ActionListener {

    private lateinit var gradle: GradleDependencyInjector

    private lateinit var btnDagger: JButton
    private lateinit var btnRetrofit: JButton
    private lateinit var btnAddRepository: JButton
    private lateinit var btnUpdateDeps: JButton
    private lateinit var cbRepos: ComboBox<String>
    private lateinit var dependencyList: JBList<String>

    override val title = "Gradle dependencies"
    override val icon = IconLoader.getIcon("/icons/tab_gradle.png")

    override fun initializePanel() {

        rootPanel = JPanel(BorderLayout())

        initialize()

        val predefinedDepsPanel = JPanel(GridLayout(5, 2, 4, 4))
        btnDagger = JButton("Add dagger")
        btnDagger.addActionListener(this)
        predefinedDepsPanel.add(btnDagger)
        btnRetrofit = JButton("Add retrofit")
        btnRetrofit.addActionListener(this)
        predefinedDepsPanel.add(btnRetrofit)

        cbRepos = ComboBox()
        cbRepos.model = DefaultComboBoxModel(gradle.displayableRepositories)
        predefinedDepsPanel.add(cbRepos)

        btnAddRepository = JButton("Add repo", IconLoader.getIcon("/icons/ic_add.png"))
        btnAddRepository.addActionListener(this)
        predefinedDepsPanel.add(btnAddRepository)
        btnUpdateDeps = JButton("Update", IconLoader.getIcon("/icons/ic_reload.png"))
        btnUpdateDeps.addActionListener(this)
        predefinedDepsPanel.add(btnUpdateDeps)

        rootPanel.add(predefinedDepsPanel, BorderLayout.WEST)

        dependencyList = JBList()
        val pluginList = JBList<String>()

        val mouseAdapter = object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {

                if (e.clickCount == 2) {
                    val idx = if (e.source === pluginList)
                        pluginList.selectedIndex
                    else
                        dependencyList.selectedIndex
                    val isPlugin = e.source === pluginList
                    gradle.injectDependency(idx, isPlugin)
                }
            }
        }

        dependencyList.setListData(gradle.displayableDependencyTitles)
        dependencyList.cellRenderer = MaterialListCellRenderer()
        dependencyList.addMouseListener(mouseAdapter)
        val dependencyScrollPane = JBScrollPane(dependencyList)
        dependencyScrollPane.border = TitledBorder(UIManager.getBorder("TitledBorder.border"), "App dependencies",
                TitledBorder.LEADING, TitledBorder.TOP, null, JBColor(0xffffff, 0xffffff))
        rootPanel.add(dependencyScrollPane, BorderLayout.CENTER)

        pluginList.setListData(gradle.displayablePluginTitles)
        pluginList.cellRenderer = MaterialListCellRenderer()
        pluginList.addMouseListener(mouseAdapter)
        val pluginScrollPane = JBScrollPane(pluginList)
        pluginScrollPane.border = TitledBorder(UIManager.getBorder("TitledBorder.border"), "Project plugins",
                TitledBorder.LEADING, TitledBorder.TOP, null, JBColor(0xffffff, 0xffffff))
        rootPanel.add(pluginScrollPane, BorderLayout.EAST)
    }

    override fun actionPerformed(e: ActionEvent) {

        when {
            e.source === btnDagger -> gradle.injectDaggerStatement()
            e.source === btnRetrofit -> gradle.injectRetrofitStatements()
            e.source == btnAddRepository -> gradle.injectRepository(cbRepos.selectedIndex)
            e.source == btnUpdateDeps -> updateDependencies()
        }
    }

    private fun initialize() {

        val p = ProjectManager.getInstance().openProjects[0]
        val rootFolder = IdeaProjectUtils.getProjectRootFolder(p)
        gradle = GradleDependencyInjector(p, rootFolder!!)
    }

    private fun updateDependencies() {

        dependencyList.setPaintBusy(true)

        gradle.updateDependencyVersions(Consumer {
            JOptionPane.showMessageDialog(rootPanel, it.localizedMessage, "Download error", JOptionPane.ERROR_MESSAGE)
        }, Action {
            dependencyList.setListData(gradle.displayableDependencyTitles)
            dependencyList.setPaintBusy(false)
        })
    }
}

