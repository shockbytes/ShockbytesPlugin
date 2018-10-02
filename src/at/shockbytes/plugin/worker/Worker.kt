package at.shockbytes.plugin.worker

import javax.swing.Icon
import javax.swing.JPanel

/**
 * Author:  Martin Macheiner
 * Date:    22.03.2016
 */
abstract class Worker {

    protected var rootPanel: JPanel = JPanel()

    val tabPanel: JPanel by lazy {
        initializePanel()
        rootPanel
    }

    abstract val title: String

    abstract val icon: Icon

    abstract fun initializePanel()

}
