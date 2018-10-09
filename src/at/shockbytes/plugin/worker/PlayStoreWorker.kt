package at.shockbytes.plugin.worker

import at.shockbytes.plugin.service.process.ProcessExecutionService
import at.shockbytes.plugin.util.IdeaProjectUtils
import at.shockbytes.plugin.view.WorkerView
import at.shockbytes.plugin.view.PlayStoreWorkerView
import com.intellij.openapi.util.IconLoader
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.lang.Exception
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
        textOutputSubject.onNext("Fastlane integration v0.1")
    }

    // ---------------------------------------------------------------------------------

    fun initializeFastlane(jsonKeyFile: String, packageName: String) {

        try {

            val fastlaneFolder = File("${IdeaProjectUtils.getOpenProject().basePath}/fastlane")

            // Make fastlane folder
            fastlaneFolder.mkdir()

            // Copy json_key.json into fastlane folder
            FileUtils.copyFile(File(jsonKeyFile), File("${fastlaneFolder.absolutePath}/json_key.json"))

            // Run `fastlane supply init --json_key fastlane/json_key.json --package_name packageName`
            processExecutionService
                    .executeCommandOnly("fastlane supply init --json_key fastlane/json_key.json --package_name $packageName")
                    .subscribe({
                        fastLaneInitializedSubject.onNext(true)
                        textOutputSubject.onNext("Fastlane is ready and set up for your project!")
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
        // TODO
        println("Configure beta lane")
    }

    fun configureReleaseLane() {
        // TODO
        println("Configure release lane")
    }

    fun publishBetaRelease() {
        // TODO
        println("Publish beta release")
    }

    fun publishRelease() {
        // TODO
        println("Publish release")
        textOutputSubject.onNext("Release published!")
    }

    fun manageScreenshots() {
        // TODO
        println("Manage screenshots")
    }

    fun manageUpdateNotes() {
        // TODO
        println("Manage update notes")
    }

    fun manageVersions() {
        // TODO
        println("Manage versions")
    }

}