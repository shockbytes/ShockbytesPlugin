package at.shockbytes.plugin.worker

import at.shockbytes.plugin.service.android.AdbService
import at.shockbytes.plugin.view.ScreenCaptureWorkerView
import at.shockbytes.plugin.view.WorkerView
import com.intellij.openapi.util.IconLoader
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.awt.Desktop

import java.io.File
import javax.swing.JPanel

/**
 * Author:  Martin Macheiner
 * Date:    31.01.2017
 */
class ScreenCaptureWorker(private val adbService: AdbService) : Worker<JPanel>() {

    private var recordedPath: String? = null

    override val title = "Screen Capture"
    override val icon = IconLoader.getIcon("/icons/tab_screen_record.png")
    override var view: WorkerView<JPanel> = ScreenCaptureWorkerView(this)

    private val btnSubject: PublishSubject<Pair<Boolean, Boolean>> = PublishSubject.create()
    private val txtSubject: PublishSubject<String> = PublishSubject.create()
    private val btnPlaySubject: PublishSubject<Boolean> = PublishSubject.create()

    // ----------------------------------------------------------------------------------

    val buttonStateObservable: Observable<Pair<Boolean, Boolean>> = btnSubject
    val textObservable: Observable<String> = txtSubject
    val buttonPlayStateObservable: Observable<Boolean> = btnPlaySubject

    private fun updateButtons(isCapturing: Boolean, onError: Boolean) {
        btnSubject.onNext(Pair(isCapturing, onError))
    }

    private fun updateTextArea(text: String) {
        txtSubject.onNext(text)
    }

    fun startScreenCapturing() {
        updateButtons(true, false)

        adbService.startScreenCapturing().subscribe({ output ->
            updateTextArea(output)
        }, { throwable ->
            throwable.printStackTrace()
            updateButtons(false, true)
            updateTextArea("${throwable.localizedMessage}\n")
        })
    }

    fun stopScreenCapturing(filePath: String) {

        adbService.stopScreenCapturing(filePath).subscribe({ (output, destination) ->
            updateButtons(false, false)
            updateTextArea(output)
            recordedPath = destination
        }, { throwable ->
            throwable.printStackTrace()
            btnPlaySubject.onNext(false)
            updateTextArea("${throwable.message}\n")
        })
    }

    fun playRecordedVideo() {

        try {
            Desktop.getDesktop().open(File(recordedPath))
            updateTextArea("Play file: $recordedPath\n")
        } catch (e: Exception) {
            e.printStackTrace()
            updateTextArea("${e.localizedMessage}\n")
        }
    }

}

