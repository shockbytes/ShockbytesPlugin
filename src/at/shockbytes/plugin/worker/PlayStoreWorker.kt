package at.shockbytes.plugin.worker

import at.shockbytes.plugin.view.WorkerView
import at.shockbytes.plugin.view.PlayStoreWorkerView
import com.intellij.openapi.util.IconLoader
import javax.swing.JPanel

/**
 * Author:  Martin Macheiner
 * Date:    01.03.2018
 */
class PlayStoreWorker: Worker<JPanel>() {

    override val title = "Play Store Publishing"

    override val icon = IconLoader.getIcon("/icons/tab_google_play.png")

    override var view: WorkerView<JPanel> = PlayStoreWorkerView(this)

}