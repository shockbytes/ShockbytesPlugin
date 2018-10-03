package at.shockbytes.plugin.worker

import at.shockbytes.plugin.view.WorkerView
import javax.swing.Icon
import javax.swing.JPanel

/**
 * Author:  Martin Macheiner
 * Date:    22.03.2016
 */
abstract class Worker<T> {

    abstract val title: String

    abstract val icon: Icon

    protected abstract var view: WorkerView<T>

    fun getView(): T = view.view

}
