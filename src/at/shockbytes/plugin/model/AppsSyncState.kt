package at.shockbytes.plugin.model

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

import java.util.HashMap

/**
 * Author:  Martin Macheiner
 * Date:    16.08.2016.
 */
@State(name = "shockbytes_apps", storages = [(Storage("shock_apps.xml"))])
class AppsSyncState : PersistentStateComponent<AppsSyncState> {

    var map: MutableMap<String, Int> = HashMap()

    override fun getState(): AppsSyncState? {
        return this
    }

    override fun loadState(recordTimeState: AppsSyncState) {
        XmlSerializerUtil.copyBean(recordTimeState, this)
    }

    fun getRevisionForApp(appName: String): Int? {
        return map[appName]
    }

    fun addApp(appName: String) {
        map[appName] = 1
    }

    fun incrementRevisionForApp(appName: String) {

        val `val` = map[appName]
        if (`val` == null) {
            map[appName] = 1
        } else {
            map[appName] = `val` + 1
        }
    }

}
