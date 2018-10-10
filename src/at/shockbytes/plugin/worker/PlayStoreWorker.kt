package at.shockbytes.plugin.worker

import at.shockbytes.plugin.service.process.ProcessExecutionService
import at.shockbytes.plugin.util.ConfigManager
import at.shockbytes.plugin.util.GradleDependencyInjector
import at.shockbytes.plugin.util.IdeaProjectUtils
import at.shockbytes.plugin.view.WorkerView
import at.shockbytes.plugin.view.PlayStoreWorkerView
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.apache.commons.io.FileUtils
import java.io.File
import java.lang.Exception
import java.lang.IllegalStateException
import javax.swing.JPanel

/**
 * Author:  Martin Macheiner
 * Date:    01.03.2018
 */
class PlayStoreWorker(private val processExecutionService: ProcessExecutionService) : Worker<JPanel>() {

    override val title = "Play Store Publishing"

    override val icon = IconLoader.getIcon("/icons/tab_google_play.png")

    override var view: WorkerView<JPanel> = PlayStoreWorkerView(this)

    private val fastLaneInitializedSubject: BehaviorSubject<Boolean> = BehaviorSubject.create()
    private val textOutputSubject: BehaviorSubject<String> = BehaviorSubject.create()

    // --------------- Clearly separate between Subjects and Observables ---------------

    val fastLaneInitializedEvent: Observable<Boolean> = fastLaneInitializedSubject
    val textOutputEvent: Observable<String> = textOutputSubject


    init {
        checkIfFastlaneIsInitialized()
    }

    private fun checkIfFastlaneIsInitialized() {

        val fastFile = File("${IdeaProjectUtils.getOpenProject().basePath}/fastlane/Fastfile")

        fastLaneInitializedSubject.onNext(fastFile.exists())
        textOutputSubject.onNext("Fastlane integration v0.4")
    }

    private fun injectGradleBuildTasks(appGradleFile: File, project: Project, gradleTasks: String) {

        val file = VfsUtil.findFileByIoFile(appGradleFile, true) ?: return
        val document = FileDocumentManager.getInstance().getDocument(file) ?: return

        CommandProcessor.getInstance().executeCommand(project, {
            ApplicationManager.getApplication().runWriteAction {

                val index = StringUtil.lastIndexOfAny(document.charsSequence, "dependencies {")
                val offset = if (index > -1) {
                    val lineNumber = document.getLineNumber(index)
                    document.getLineEndOffset(lineNumber - 1)
                } else -1

                if (offset > -1) {
                    document.insertString(offset, gradleTasks)
                }
            }
        }, "Add fastlane specific gradle tasks to build.gradle file", null)
    }

    private fun executeCommand(command: String) {
        processExecutionService
                .executeCommand(command)
                .subscribe({ output ->
                    textOutputSubject.onNext(output)
                }, { throwable -> textOutputSubject.onNext(throwable.message) })
    }

    // ---------------------------------------------------------------------------------

    fun initializeFastlane(jsonKeyFile: String, packageName: String) {

        try {

            val projectRootFolder = IdeaProjectUtils.getOpenProject().basePath
                    ?: throw IllegalStateException("Root Folder must not be null!")
            val fastlaneFolder = File("$projectRootFolder/fastlane")
            val versionPropsFile = File("$projectRootFolder/app/version.properties")
            val appGradleFile = File("$projectRootFolder/app/build.gradle")

            // Make fastlane folder
            fastlaneFolder.mkdir()

            // Copy json_key.json into fastlane folder
            FileUtils.copyFile(File(jsonKeyFile), File("${fastlaneFolder.absolutePath}/google_play.json"))

            // Create the default FastFile
            val fastFile = File("${fastlaneFolder.absolutePath}/Fastfile")
            if (!fastFile.exists()) {
                fastFile.createNewFile()
                fastFile.bufferedWriter().use {
                    it.write(ConfigManager.getTemplateFileContent("FastfileTemplate"))
                }
            }

            // Copy gradle tasks into build.gradle
            injectGradleBuildTasks(appGradleFile, IdeaProjectUtils.getOpenProject(),
                    ConfigManager.getTemplateFileContent("FastlaneGradleTemplate.txt"))

            // (Optionally) Create versions.properties file
            if (!versionPropsFile.exists()) {
                versionPropsFile.createNewFile()
                versionPropsFile.bufferedWriter().use {
                    it.write(ConfigManager.getTemplateFileContent("VersionPropertiesTemplate.txt"))
                }
            }

            // Run `fastlane supply init --json_key fastlane/json_key.json --package_name packageName`
            processExecutionService
                    .executeCommand("fastlane supply init --json_key $projectRootFolder/fastlane/google_play.json --package_name $packageName")
                    .subscribe({

                        fastLaneInitializedSubject.onNext(true)
                        textOutputSubject.onNext("$it${System.lineSeparator()}${System.lineSeparator()}Fastlane successfully set up and running!")

                    }, { throwable ->
                        fastLaneInitializedSubject.onNext(false)
                        textOutputSubject.onNext("Cannot initialize fastlane: ${throwable.localizedMessage}")
                    })

        } catch (e: Exception) {
            fastLaneInitializedSubject.onNext(false)
            textOutputSubject.onNext("Cannot initialize fastlane: ${e.localizedMessage}")
        }
    }

    fun configureBetaLane() {
        textOutputSubject.onNext("Configuring beta lane will come with a later release...")
    }

    fun configureReleaseLane() {
        textOutputSubject.onNext("Configuring release lane will come with a later release...")
    }

    fun publishBetaRelease() {
        executeCommand("fastlane beta")
    }

    fun publishRelease() {
        executeCommand("fastlane playstore")
    }

    fun manageScreenshots() {
        // TODO
        println("Manage screenshots")
    }

    fun manageReleaseNotes() {
        // TODO
        println("Manage release notes")
    }

    fun manageVersions() {
        // TODO
        println("Manage versions")
    }

    fun prepareForRelease() {

        val projectRootFolder = IdeaProjectUtils.getOpenProject().basePath
                ?: throw IllegalStateException("Root Folder must not be null!")
        val cmd = "$projectRootFolder/gradlew fastlanePreparation"

        executeCommand(cmd)
    }

}