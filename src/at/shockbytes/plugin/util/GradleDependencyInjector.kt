package at.shockbytes.plugin.util

import at.shockbytes.plugin.service.dependency.GradleDependencyResolveService
import at.shockbytes.plugin.service.dependency.model.GradleDependency
import at.shockbytes.plugin.service.dependency.model.GradlePluginStatement
import at.shockbytes.plugin.service.dependency.model.GradleRepository
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.JavaDirectoryService
import com.intellij.psi.PsiManager
import io.reactivex.Observable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException

/**
 * Author:  Mescht
 * Date:    21.02.2017
 */
class GradleDependencyInjector(private val project: Project, rootFolder: String,
                               loadStatements: Boolean = true) {

    private val projectName: String = project.name

    private var projectGradleFile: String? = null
    private var appGradleFile: String? = null
    private var mainActivityFile: String? = null
    private var manifestFile: String? = null

    private var gradleStatements: MutableList<GradleDependency> = mutableListOf()
    private var pluginStatements: List<GradlePluginStatement> = listOf()
    private var daggerStatements: List<GradleDependency> = listOf()
    private var retrofitStatements: List<GradleDependency> = listOf()
    private var repositories: List<GradleRepository> = listOf()

    private var depsResolveService: GradleDependencyResolveService = GradleDependencyResolveService()

    val displayableDependencyTitles: Array<String>
        get() = gradleStatements
                .map { it.qualifiedTitle }
                .toTypedArray()

    val displayablePluginTitles: Array<String>
        get() = pluginStatements
                .map { it.qualifiedTitle }
                .toTypedArray()

    val displayableRepositories: Array<String>
        get() = repositories
                .map { it.title }
                .toTypedArray()

    private enum class AppGradlePosition {
        DEPENDENCY, TOP, BOTTOM
    }

    private enum class ProjectGradlePosition {
        PLUGIN, REPOSITORY
    }

    init {
        grabImportantProjectFiles(rootFolder)

        // Disable for testing
        if (loadStatements) {
            loadGradleStatements()
        }
    }

    // --------------------------------------------------------------------------------

    fun injectDependency(index: Int, isPlugin: Boolean) {

        if (isPlugin) {
            injectPluginStatement(pluginStatements[index])
        } else {
            injectDependencyStatement(gradleStatements[index])
        }
    }

    fun injectDaggerStatement() {
        injectDependencyStatements(daggerStatements)
        createDaggerFiles()
    }

    fun injectRetrofitStatements() {
        injectDependencyStatements(retrofitStatements)
    }

    fun injectRepository(repoIndex: Int) {
        val repo = repositories[repoIndex]
        injectRepositoryStatement("\n\t\t" + repo.statement)
    }

    fun updateDependencyVersions(onError: Consumer<Throwable>, onComplete: Action) {

        Observable.zipIterable(gradleStatements.map { depsResolveService.resolveDependencyVersion(it, it.endPoint) },
                { it: Array<Any> ->
                    gradleStatements.clear()
                    it.mapTo(gradleStatements) { it as GradleDependency }
                }, false, 20)
                .subscribeOn(Schedulers.io())
                .subscribe(Consumer {
                    ConfigManager.storeGradleDependencies(it).subscribe { println("Dependency file updated!") }
                }, onError, onComplete)
    }

    fun verifyFiles(): Boolean {
        return projectGradleFile != null && appGradleFile != null && mainActivityFile != null && manifestFile != null
    }

    // --------------------------------------------------------------------------------

    private fun createDaggerFiles() {

        // TODO Fix this, Activities are now located in .ui.activity
        /* Do a little hack
        *
        * 1. Find MainActivity (we assume we have always a MainActivity in .core package
        * 2. Switch one level back and create a new PsiDirectory called .dagger
        * 3. Create AppComponent and AppModule classes
        * 4. Fill them with the actual code
        * 5. Create in .core the PROJECTNAME_Application class
        * 6. Add the android:label=".core.PROJECTNAME_Application" tag in the manifest
        *
        */

        CommandProcessor.getInstance().executeCommand(project, {
            ApplicationManager.getApplication().runWriteAction {

                val appName = projectName + "App"
                val mainFile = VfsUtil.findFileByIoFile(File(mainActivityFile), true)
                val coreDirectory = PsiManager.getInstance(project).findDirectory(mainFile?.parent!!)
                val daggerDirectory = coreDirectory?.parentDirectory?.createSubdirectory("dagger")

                val appModuleClass = JavaDirectoryService.getInstance().createClass(daggerDirectory!!, "AppModule")
                val appComponentClass = JavaDirectoryService.getInstance().createInterface(daggerDirectory, "AppComponent")
                val appClass = JavaDirectoryService.getInstance().createClass(coreDirectory, appName)

                DaggerCodeGenerator.modifyAppComponent(appComponentClass.containingFile.virtualFile, project)
                DaggerCodeGenerator.modifyAppModule(appModuleClass.containingFile.virtualFile, project)
                DaggerCodeGenerator.modifyApplicationFile(appClass.containingFile.virtualFile, project, appName)
                DaggerCodeGenerator.modifyManifest(VfsUtil.findFileByIoFile(File(manifestFile), true)!!, project, appName)

            }
        }, "Create dagger files", null)
    }

    private fun grabImportantProjectFiles(rootFolder: String) {

        File(rootFolder).walk()
                .filter {
                    !it.absolutePath.contains("build\\intermediates")
                            && (it.name == "build.gradle" || it.name == "MainActivity.java" || it.name == "AndroidManifest.xml")
                }
                .forEach {
                    val filename = it.name
                    val path = it.absolutePath
                    val parent = it.parentFile.name
                    when {
                        filename == "MainActivity.java" -> mainActivityFile = path
                        filename == "AndroidManifest.xml" -> manifestFile = path
                        parent == "app" || parent == "mobile" -> appGradleFile = path
                        parent == projectName -> projectGradleFile = path
                    }
                }
    }

    // ----------------------------------------------------------------------------------------

    private fun injectPluginStatement(statement: GradlePluginStatement) {

        val pluginStatement = "\n\t\t" + statement.qualifiedStatement
        injectIntoProjectGradleFile(pluginStatement, ProjectGradlePosition.PLUGIN)

        val s = "\n" + statement.applyName
        val position = if (statement.isApplyTop) AppGradlePosition.TOP else AppGradlePosition.BOTTOM
        injectIntoAppGradleFile(s, position)
    }

    private fun injectDependencyStatement(statement: GradleDependency) {
        val s = "\n\t" + statement.qualifiedStatement
        injectIntoAppGradleFile(s, AppGradlePosition.DEPENDENCY)
    }

    private fun injectDependencyStatements(statements: List<GradleDependency>) {
        val statement = "\n" + statements.joinToString("\n") { s -> "\t" + s.qualifiedStatement }
        injectIntoAppGradleFile(statement, AppGradlePosition.DEPENDENCY)
    }

    private fun injectRepositoryStatement(statement: String) {
        injectIntoProjectGradleFile(statement, ProjectGradlePosition.REPOSITORY)
    }

    private fun injectIntoAppGradleFile(statement: String, position: AppGradlePosition) {

        val file = VfsUtil.findFileByIoFile(File(appGradleFile), true) ?: return
        val document = FileDocumentManager.getInstance().getDocument(file) ?: return

        CommandProcessor.getInstance().executeCommand(project, {
            ApplicationManager.getApplication().runWriteAction {

                val offset = when (position) {

                    AppGradlePosition.DEPENDENCY -> {
                        val index = StringUtil.lastIndexOfAny(document.charsSequence, "}")
                        if (index > -1) {
                            val lineNumber = document.getLineNumber(index)
                            document.getLineEndOffset(lineNumber - 1)
                        } else -1
                    }
                    AppGradlePosition.TOP -> document.getLineEndOffset(0)
                    AppGradlePosition.BOTTOM -> document.getLineEndOffset(document.lineCount - 1)
                }

                if (offset > -1) {
                    document.insertString(offset, statement)
                }
            }
        }, "Update app build.gradle file", null)
    }

    private fun injectIntoProjectGradleFile(statement: String, position: ProjectGradlePosition) {

        val file = VfsUtil.findFileByIoFile(File(projectGradleFile), true) ?: return
        val document = FileDocumentManager.getInstance().getDocument(file) ?: return

        CommandProcessor.getInstance().executeCommand(project, {
            ApplicationManager.getApplication().runWriteAction {

                val startIdx: Int = when (position) {
                    GradleDependencyInjector.ProjectGradlePosition.PLUGIN -> 0
                    GradleDependencyInjector.ProjectGradlePosition.REPOSITORY -> StringUtil.indexOf(document.charsSequence, "allprojects")
                }
                val infix: String = when (position) {
                    GradleDependencyInjector.ProjectGradlePosition.PLUGIN -> "classpath"
                    GradleDependencyInjector.ProjectGradlePosition.REPOSITORY -> "repositories"
                }

                val index = StringUtil.indexOf(document.charsSequence, infix, startIdx)
                if (index > -1) {
                    val lineNumber = document.getLineNumber(index)
                    val offset = document.getLineEndOffset(lineNumber)
                    document.insertString(offset, statement)
                }
            }
        }, "Update project build.gradle file", null)
    }

    // ----------------------------------------------------------------------------------------

    private fun loadGradleStatements() {

        try {

            val depsString = ConfigManager.loadGradleDependencies()
            val jsonObject = JsonParser().parse(depsString).asJsonObject

            val generalDependencies = jsonObject.get("dependencies").asJsonArray
            val pluginDependencies = jsonObject.get("plugins").asJsonArray
            val daggerDependencies = jsonObject.get("dagger").asJsonArray
            val retrofitDependencies = jsonObject.get("retrofit").asJsonArray
            val repositoryDependencies = jsonObject.get("repositories").asJsonArray

            // Load general dependencies
            gradleStatements = loadDependencies(generalDependencies)
            // Load plugin statements
            pluginStatements = loadPlugins(pluginDependencies)
            // Load dagger dependencies
            daggerStatements = loadDependencies(daggerDependencies)
            // Load retrofit dependencies
            retrofitStatements = loadDependencies(retrofitDependencies)
            // Load repository dependencies
            repositories = loadRepositories(repositoryDependencies)

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun loadDependencies(array: JsonArray): MutableList<GradleDependency> {
        return array.mapTo(mutableListOf()) {

            val obj = it.asJsonObject

            val title = obj.get("title").asString
            val statement = obj.get("statement").asString
            val version = obj.get("version").asString
            val query = obj.get("query").asString
            val endPoint = GradleDependency.EndPoint.valueOf(obj.get("endpoint").asString.toUpperCase())

            GradleDependency(title, statement, version, query, endPoint)
        }
    }

    private fun loadRepositories(array: JsonArray): MutableList<GradleRepository> {
        return array.mapTo(mutableListOf()) {

            val obj = it.asJsonObject

            val title = obj.get("title").asString
            val statement = obj.get("statement").asString

            GradleRepository(title, statement)
        }
    }

    private fun loadPlugins(array: JsonArray): MutableList<GradlePluginStatement> {
        return array.mapTo(mutableListOf()) {
            val obj = it.asJsonObject

            val title = obj.get("title").asString
            val statement = obj.get("statement").asString
            val version = obj.get("version").asString
            val applyTop = obj.get("applyTop").asBoolean
            val applyName = obj.get("applyName").asString

            GradlePluginStatement(title, statement, version, applyTop, applyName)
        }
    }

}
