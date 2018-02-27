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
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

/**
 * Author:  Mescht
 * Date:    21.02.2017
 */
class GradleDependencyInjector(private val project: Project, rootFolder: String) {

    private val projectName: String = project.name

    private var projectGradleFile: String? = null
    private var appGradleFile: String? = null
    private var mainActivityFile: String? = null
    private var manifestFile: String? = null

    private var gradleStatements: MutableList<GradleDependency> = mutableListOf()
    private var pluginStatements: MutableList<GradlePluginStatement> = mutableListOf()
    private var daggerStatements: MutableList<GradleDependency> = mutableListOf()
    private var retrofitStatements: MutableList<GradleDependency> = mutableListOf()
    private var repositories: MutableList<GradleRepository> = mutableListOf()

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

    private enum class InsertionPosition {
        DEPENDENCY, TOP, BOTTOM
    }

    init {
        grabImportantProjectFiles(rootFolder)
        loadGradleStatements()
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
        injectRepositoryStatement("\n\t" + repo.statement)
    }

    fun updateDependencyVersions(onError: Consumer<Throwable>, onComplete: Action) {

        Observable.zipIterable(gradleStatements.map { depsResolveService.resolveDependencyVersion(it, it.endPoint) },
                { it: Array<Any> ->
                    gradleStatements.clear()
                    it.mapTo(gradleStatements) { it as GradleDependency }
                }, false, 20)
                .subscribeOn(Schedulers.io())
                .subscribe(Consumer { }, onError, onComplete)
    }

    // --------------------------------------------------------------------------------

    private fun createDaggerFiles() {

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

        try {
            val files = Files.walk(Paths.get(rootFolder))
                    .parallel()
                    .filter { path ->
                        val filename = path.fileName.toString()
                        (filename == "build.dependency" || filename == "MainActivity.java"
                                || filename == "AndroidManifest.xml")
                    }
                    .map { p -> p.toAbsolutePath().toString() }
                    .collect(Collectors.toList())

            files.forEach { s ->
                val filename = Paths.get(s).parent.fileName.toString()
                when {
                    filename.equals(projectName, ignoreCase = true) -> projectGradleFile = s
                    filename.equals("app", ignoreCase = true) -> appGradleFile = s
                    s.endsWith("MainActivity.java") -> mainActivityFile = s
                    s.endsWith("AndroidManifest.xml") -> manifestFile = s
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    // ----------------------------------------------------------------------------------------

    private fun injectPluginStatement(statement: GradlePluginStatement) {

        val pluginStatement = "\n\t\t" + statement.qualifiedStatement
        injectProjectPluginStatement(pluginStatement)

        val s = "\n" + statement.applyName
        val position = if (statement.isApplyTop) InsertionPosition.TOP else InsertionPosition.BOTTOM
        injectAppStatement(s, position)
    }

    private fun injectDependencyStatement(statement: GradleDependency) {
        val s = "\n\t" + statement.qualifiedStatement
        injectAppStatement(s, InsertionPosition.DEPENDENCY)
    }

    private fun injectDependencyStatements(statements: List<GradleDependency>) {
        val statement = "\n" + statements.joinToString("\n") { s -> "\t" + s.qualifiedStatement }
        injectAppStatement(statement, InsertionPosition.DEPENDENCY)
    }

    private fun injectRepositoryStatement(statement: String) {
        // TODO
    }

    private fun injectAppStatement(statement: String, position: InsertionPosition) {

        val file = VfsUtil.findFileByIoFile(File(appGradleFile), true)
        if (file != null) {
            val document = FileDocumentManager.getInstance().getDocument(file)
                    ?: return  // can't read the file. Ex: it is too big

            CommandProcessor.getInstance().executeCommand(project, {
                ApplicationManager.getApplication().runWriteAction {

                    val offset = when (position) {

                        InsertionPosition.DEPENDENCY -> {

                            val index = StringUtil.lastIndexOfAny(document.charsSequence, "}")
                            if (index == -1) {
                                return@runWriteAction
                            }
                            val lineNumber = document.getLineNumber(index)
                            document.getLineEndOffset(lineNumber - 1)
                        }
                        InsertionPosition.TOP -> document.getLineEndOffset(0)
                        InsertionPosition.BOTTOM -> document.getLineEndOffset(document.lineCount - 1)
                    }

                    document.insertString(offset, statement)

                }
            }, "Update app build.dependency", null)
        }
    }

    private fun injectProjectPluginStatement(statement: String) {

        val file = VfsUtil.findFileByIoFile(File(projectGradleFile), true)
        if (file != null) {
            val document = FileDocumentManager.getInstance().getDocument(file)
                    ?: return  // can't read the file. Ex: it is too big

            CommandProcessor.getInstance().executeCommand(project, {
                ApplicationManager.getApplication().runWriteAction {

                    val index = StringUtil.indexOf(document.charsSequence, "classpath")
                    if (index == -1) {
                        return@runWriteAction
                    }
                    val lineNumber = document.getLineNumber(index)
                    val offset = document.getLineEndOffset(lineNumber)

                    document.insertString(offset, statement)
                }
            }, "Update project build.dependency", null)
        }

    }

    private fun loadGradleStatements() {

        try {

            // Read file
            val inStream = javaClass.getResourceAsStream("/gradle_dependencies.json")
            val depsString = IOUtils.toString(inStream, "UTF-8")
            inStream.close()

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
