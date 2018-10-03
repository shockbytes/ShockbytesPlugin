package at.shockbytes.plugin


import at.shockbytes.plugin.platform.PlatformManager
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

    private val platformManager: PlatformManager = PlatformManager.forPlatform(System.getProperty("os.name"))

    private val worker by lazy {
        listOf(
                AndroidWorker(platformManager.adbService,
                        platformManager.certificateService,
                        platformManager.pushService,
                        platformManager.googleDriveOptions),
                WorkspaceCrawlerWorker(platformManager.workSpaceCrawler),
                GradleWorker(),
                ScreenCaptureWorker(platformManager.adbService),
                PlayStoreWorker())
    }

    private var tabbedPane: JBTabbedPane = JBTabbedPane()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        setupViews(toolWindow.component)
        createTabs()
    }

    private fun setupViews(container: Container) {

        // Style the JBList
        UIManager.put("List.focusCellHighlightBorder", BorderFactory.createEmptyBorder())

        // Add TabbedPane to the PluginWindow
        container.add(tabbedPane)
    }

    private fun createTabs() {
        worker.forEach { tabbedPane.addTab(it.title, it.icon, it.getView()) }
    }

}
