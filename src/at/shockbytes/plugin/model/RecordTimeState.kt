package at.shockbytes.plugin.model

import at.shockbytes.plugin.util.HelperUtil
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

import java.util.ArrayList

/**
 * Author:  Martin Macheiner
 * Date:    16.08.2016.
 */
@State(name = "shockbytes_time_records", storages = arrayOf(Storage("shock_time_records.xml")))
class RecordTimeState : PersistentStateComponent<RecordTimeState> {

    var projects: List<Project> = ArrayList()

    val projectListData: Array<String>
        get() {
            val data = arrayOf<String>()
            for (i in data.indices) {
                val p = projects[i]
                data[i] = p.name + "\t / \t" + p.workingHours + "h"
            }
            return data
        }

    override fun getState(): RecordTimeState? {
        return this
    }

    override fun loadState(recordTimeState: RecordTimeState) {
        XmlSerializerUtil.copyBean(recordTimeState, this)
    }

    fun getTimeRecordEntryListData(selectedProject: Int): Array<String> {

        val p = getProjectByIndex(selectedProject) ?: return arrayOf()
        val data = arrayOf<String>()
        for (i in data.indices) {
            val entry = p.records[i]
            data[i] = entry.toListDataString()
        }
        return data
    }

    fun addEntryToProject(selectedProject: Int, entry: TimeRecordEntry) {
        val p = getProjectByIndex(selectedProject) ?: return
        p.addTimeRecordEntry(entry)
    }

    fun removeEntriesFromProject(selectedProject: Int, selectedIndices: IntArray?) {

        val p = getProjectByIndex(selectedProject)
        if (p == null || selectedIndices == null || selectedIndices.isEmpty()) {
            return
        }
        for (i in selectedIndices.indices.reversed()) {
            val entry = p.records.removeAt(selectedIndices[i])
            p.workingMinutes -= entry.minutes
        }
    }

    fun getProjectByName(name: String): Project? {
        return projects.firstOrNull { it.name == name }
    }

    fun getProjectByIndex(selectedProject: Int): Project? {
        return if (selectedProject < 0 || selectedProject >= projects.size) {
            null
        } else projects[selectedProject]
    }

    class TimeRecordEntry @JvmOverloads constructor(var minutes: Int = 0, var start: Long = 0, var end: Long = 0) {

        fun toListDataString(): String {
            return minutes.toString() + "min (" + HelperUtil.formatDate(start) + " " + HelperUtil.formatTime(start) + " - " + HelperUtil.formatTime(end) + ")"
        }

        override fun toString(): String {

            var str = "Minutes:\t" + minutes
            str += "\nStart:\t" + start
            str += "\nEnd:\t" + end
            return str
        }
    }

    class Project(var name: String = "") {
        var workingMinutes: Int = 0

        var records: MutableList<TimeRecordEntry> = ArrayList()

        val workingHours: Int
            get() = workingMinutes / 60

        fun addTimeRecordEntry(entry: TimeRecordEntry) {
            records.add(entry)
            workingMinutes += entry.minutes
        }
    }

    override fun toString(): String {

        var str = "RecordTimeState\n"
        for (p in projects) {
            str += "Project " + p.name + " / Hours: " + p.workingMinutes + "\n"
            for (entry in p.records) {
                str += entry.toString()
            }
            str += "\n-------------\n\n"
        }

        return str
    }
}
