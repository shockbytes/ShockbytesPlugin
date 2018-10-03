package at.shockbytes.plugin.worker

import at.shockbytes.plugin.util.GradleDependencyInjector
import at.shockbytes.plugin.util.IdeaProjectUtils
import at.shockbytes.plugin.view.GradleWorkerView
import at.shockbytes.plugin.view.WorkerView
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.IconLoader
import io.reactivex.Observable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.lang.IllegalStateException
import javax.swing.JOptionPane
import javax.swing.JPanel

/**
 * Author:  Martin Macheiner
 * Date:    21.02.2017
 */
class GradleWorker : Worker<JPanel>() {

    override val title = "Gradle dependencies"
    override val icon = IconLoader.getIcon("/icons/tab_gradle.png")
    override var view: WorkerView<JPanel> = GradleWorkerView(this)

    private lateinit var gradle: GradleDependencyInjector

    private val displayableDependenciesSubject: BehaviorSubject<Array<String>> = BehaviorSubject.create()
    private val displayablePluginSubject: BehaviorSubject<Array<String>> = BehaviorSubject.create()
    private val displayableRepositoriesSubject: BehaviorSubject<Array<String>> = BehaviorSubject.create()

    private val updateDependenciesErrorSubject: PublishSubject<Throwable> = PublishSubject.create()
    private val updateDependenciesCompletableSubject: PublishSubject<Array<String>> = PublishSubject.create()

    // --------------- Clearly separate between PublishSubject and Observable ---------------

    val displayableDependenciesObservable: Observable<Array<String>> = displayableDependenciesSubject
    val displayablePluginsObservable: Observable<Array<String>> = displayablePluginSubject
    val displayableRepositoriesObservable: Observable<Array<String>> = displayableRepositoriesSubject

    val updateDependenciesErrorObservable: Observable<Throwable> = updateDependenciesErrorSubject
    val updateDependenciesCompletableObservable: Observable<Array<String>> = updateDependenciesCompletableSubject

    init {
        initialize()
    }

    private fun initialize() {
        val p = ProjectManager.getInstance().openProjects[0]
        val rootFolder = IdeaProjectUtils.getProjectRootFolder(p) ?: throw IllegalStateException("Rootfolder must not be null!")
        gradle = GradleDependencyInjector(p, rootFolder)

        // Update the view
        displayableRepositoriesSubject.onNext(gradle.displayableRepositories)
        displayableDependenciesSubject.onNext(gradle.displayableDependencyTitles)
        displayablePluginSubject.onNext(gradle.displayablePluginTitles)
    }

    fun injectDaggerStatements() {
        gradle.injectDaggerStatements()
    }

    fun injectRetrofitStatements() {
        gradle.injectRetrofitStatements()
    }

    fun injectRepository(repoIdx: Int) {
        gradle.injectRepository(repoIdx)
    }

    fun injectDependency(idx: Int, isPlugin: Boolean) {
        gradle.injectDependency(idx, isPlugin)
    }

    fun updateDependencies() {
        gradle.updateDependencyVersions(
                Consumer { t -> updateDependenciesErrorSubject.onNext(t) },
                Action { updateDependenciesCompletableSubject.onNext(gradle.displayableDependencyTitles) })
    }
}

