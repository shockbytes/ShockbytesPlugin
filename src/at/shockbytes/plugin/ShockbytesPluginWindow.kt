package at.shockbytes.plugin


import at.shockbytes.plugin.worker.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBTabbedPane
import java.awt.Container
import javax.swing.BorderFactory
import javax.swing.UIManager

/**
 * Author:  Martin Macheiner
 * Date:    22.03.2016.
 */
class ShockbytesPluginWindow : ToolWindowFactory {

    private lateinit var worker: List<Worker>

    private var tabbedPane: JBTabbedPane = JBTabbedPane()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        setupViews()
        initializeWorker()
        createTabs(toolWindow.component)
    }

    private fun setupViews() {
        UIManager.put("List.focusCellHighlightBorder", BorderFactory.createEmptyBorder())
    }

    private fun initializeWorker() {
        worker = listOf(AndroidWorker(),
                WorkspaceCrawlerWorker(),
                GradleWorker(),
                ScreenCaptureHelper(),
                PlayStoreWorker())
    }

    private fun createTabs(container: Container) {
        worker.forEach { w -> tabbedPane.addTab(w.title, w.icon, w.tabPanel) }
        container.add(tabbedPane)
    }

    // --------------------------------------------------------------------

    companion object {

        const val SHOCKBYTES_TITLE = "Shockbytes v3.0"
    }

}
