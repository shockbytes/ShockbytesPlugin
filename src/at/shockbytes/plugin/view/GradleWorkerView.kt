package at.shockbytes.plugin.view

import at.shockbytes.plugin.ui.MaterialListCellRenderer
import at.shockbytes.plugin.util.addTo
import at.shockbytes.plugin.worker.GradleWorker
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import io.reactivex.disposables.CompositeDisposable
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.TitledBorder

class GradleWorkerView(private val gradleWorker: GradleWorker): WorkerView<JPanel>(), ActionListener {

    private lateinit var btnDagger: JButton
    private lateinit var btnRetrofit: JButton
    private lateinit var btnAddRepository: JButton
    private lateinit var btnUpdateDeps: JButton
    private lateinit var cbRepos: ComboBox<String>
    private lateinit var dependencyList: JBList<String>
    private lateinit var pluginList: JBList<String>

    override fun initializeView(): JPanel {

        val rootPanel = JPanel(BorderLayout())

        val predefinedDepsPanel = JPanel(GridLayout(5, 2, 4, 4))
        btnDagger = JButton("Add dagger")
        btnDagger.addActionListener(this)
        predefinedDepsPanel.add(btnDagger)
        btnRetrofit = JButton("Add retrofit")
        btnRetrofit.addActionListener(this)
        predefinedDepsPanel.add(btnRetrofit)

        cbRepos = ComboBox()
        predefinedDepsPanel.add(cbRepos)

        btnAddRepository = JButton("Add repo", IconLoader.getIcon("/icons/ic_add.png"))
        btnAddRepository.addActionListener(this)
        predefinedDepsPanel.add(btnAddRepository)
        btnUpdateDeps = JButton("Update", IconLoader.getIcon("/icons/ic_reload.png"))
        btnUpdateDeps.addActionListener(this)
        predefinedDepsPanel.add(btnUpdateDeps)

        rootPanel.add(predefinedDepsPanel, BorderLayout.WEST)

        dependencyList = JBList()
        pluginList = JBList<String>()

        val mouseAdapter = object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {

                if (e.clickCount == 2) {
                    val idx = if (e.source === pluginList)
                        pluginList.selectedIndex
                    else
                        dependencyList.selectedIndex
                    val isPlugin = e.source === pluginList
                    gradleWorker.injectDependency(idx, isPlugin)
                }
            }
        }

        dependencyList.cellRenderer = MaterialListCellRenderer()
        dependencyList.addMouseListener(mouseAdapter)
        val dependencyScrollPane = JBScrollPane(dependencyList)
        dependencyScrollPane.border = TitledBorder(UIManager.getBorder("TitledBorder.border"), "App dependencies",
                TitledBorder.LEADING, TitledBorder.TOP, null, JBColor(0xffffff, 0xffffff))
        rootPanel.add(dependencyScrollPane, BorderLayout.CENTER)

        pluginList.cellRenderer = MaterialListCellRenderer()
        pluginList.addMouseListener(mouseAdapter)
        val pluginScrollPane = JBScrollPane(pluginList)
        pluginScrollPane.border = TitledBorder(UIManager.getBorder("TitledBorder.border"), "Project plugins",
                TitledBorder.LEADING, TitledBorder.TOP, null, JBColor(0xffffff, 0xffffff))
        rootPanel.add(pluginScrollPane, BorderLayout.EAST)

        return rootPanel
    }

    override fun observeWorker(): CompositeDisposable? {
        val disposables = CompositeDisposable()

        gradleWorker.updateDependenciesCompletableObservable.subscribe { displayableDependencyTitles ->
            dependencyList.setListData(displayableDependencyTitles)
            dependencyList.setPaintBusy(false)
        }.addTo(disposables)

        gradleWorker.updateDependenciesErrorObservable.subscribe { t ->
            JOptionPane.showMessageDialog(dependencyList, t.localizedMessage, "Download error", JOptionPane.ERROR_MESSAGE)
        }.addTo(disposables)

        gradleWorker.displayableDependenciesObservable.subscribe { displayableDependencyTitles ->
            dependencyList.setListData(displayableDependencyTitles)
        }.addTo(disposables)

        gradleWorker.displayablePluginsObservable.subscribe { displayablePluginTitles ->
            pluginList.setListData(displayablePluginTitles)
        }.addTo(disposables)

        gradleWorker.displayableRepositoriesObservable.subscribe { displayableRepositories ->
            cbRepos.model = DefaultComboBoxModel(displayableRepositories)
        }.addTo(disposables)


        return disposables
    }


    override fun actionPerformed(e: ActionEvent) {

        when {
            e.source === btnDagger -> gradleWorker.injectDaggerStatements()
            e.source === btnRetrofit -> gradleWorker.injectRetrofitStatements()
            e.source == btnAddRepository -> gradleWorker.injectRepository(cbRepos.selectedIndex)
            e.source == btnUpdateDeps -> {
                dependencyList.setPaintBusy(true)
                gradleWorker.updateDependencies()
            }
        }
    }
}