package at.shockbytes.plugin.worker

import com.intellij.openapi.util.IconLoader
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * Author:  Martin Macheiner
 * Date:    01.03.2018
 */
class PlayStoreWorker: Worker() {

    override val title = "Play Store Publishing"
    override val icon = IconLoader.getIcon("/icons/tab_google_play.png")

    override fun initializePanel() {

        rootPanel = JPanel(BorderLayout())
        rootPanel.add(JLabel("Use Fastlane/Supply for faster publishing - Coming in Version 3.2"), BorderLayout.CENTER)
    }

}