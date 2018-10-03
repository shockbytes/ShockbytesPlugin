package at.shockbytes.plugin.worker

import at.shockbytes.plugin.service.workspace.CrawlOptions
import at.shockbytes.plugin.service.workspace.WorkspaceCrawler
import at.shockbytes.plugin.util.IdeaProjectUtils
import at.shockbytes.plugin.view.WorkerView
import at.shockbytes.plugin.view.WorkspaceCrawlerWorkerView
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.CollectionComboBoxModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.awt.Component
import java.awt.Desktop
import java.awt.GridLayout
import java.awt.Point
import java.awt.event.MouseEvent
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.swing.*


/**
 * Author:  Martin Macheiner
 * Date:    09.01.2017
 *
 * --> Kotlin implementation for file walking
 * val files = File(workspaceDirectory).walk().maxDepth(15)
 * .filter { fileFilter(it, keyword) }
 * .map { it.absolutePath }
 * .toList().toTypedArray()
 */
class WorkspaceCrawlerWorker(private val crawler: WorkspaceCrawler) : Worker<JPanel>() {

    override val title = "Workspace Crawler"
    override val icon = IconLoader.getIcon("/icons/tab_workspace_crawler.png")
    override var view: WorkerView<JPanel> = WorkspaceCrawlerWorkerView(this)

    private val crawlSubject: PublishSubject<Triple<Array<String>, String, Long>> = PublishSubject.create()
    private val crawlErrorSubject: PublishSubject<Throwable> = PublishSubject.create()

    // --------------- Clearly separate between PublishSubject and Observable ---------------

    val crawlObservable: Observable<Triple<Array<String>, String, Long>> = crawlSubject
    val crawlErrorObservable: Observable<Throwable> = crawlErrorSubject

    private fun copyIntoWorkspace(file: File, point: Point) {

        val projects = ProjectManager.getInstance().openProjects
        if (projects.isNotEmpty()) {
            showCopyDialog(file, point,
                    IdeaProjectUtils.getPackagesFromProject(projects[0]),
                    IdeaProjectUtils.getSourceRootFolder(projects[0]))
        } else {
            JOptionPane.showMessageDialog(view.view, "There is no open project!")
        }
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
        val comboBox = ComboBox(CollectionComboBoxModel<String>(IdeaProjectUtils.getDisplayablePackageName(packages)))
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

    fun crawl(keyword: String, crawlOptions: CrawlOptions) {

        var start = 0L
        crawler.crawl(keyword, crawlOptions)
                .doOnSubscribe { start = System.currentTimeMillis() }
                .subscribe({ (keyword, files) ->
                    val duration = (System.currentTimeMillis() - start) / 1000L
                    crawlSubject.onNext(Triple(files, keyword, duration))
                }, { throwable -> crawlErrorSubject.onNext(throwable) })
    }

    fun showPopup(component: Component, e: MouseEvent?, filename: String) {

        val menu = JPopupMenu()
        val itemOpen = JMenuItem("Open folder in explorer")
        itemOpen.icon = IconLoader.getIcon("/icons/ic_open_in_explorer.png")
        itemOpen.addActionListener { openFile(File(filename).parentFile) }
        menu.add(itemOpen)
        val itemCopy = JMenuItem("Copy to workspace")
        itemCopy.icon = IconLoader.getIcon("/icons/ic_copy.png")
        itemCopy.addActionListener { copyIntoWorkspace(File(filename), e?.locationOnScreen ?: Point(0, 0)) }
        menu.add(itemCopy)

        menu.show(component, e?.point?.x ?: 0, e?.point?.y ?: 0)
    }

    fun openFile(f: File) {
        try {
            Desktop.getDesktop().open(f)
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
    }

}
