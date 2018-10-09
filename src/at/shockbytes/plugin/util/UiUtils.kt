package at.shockbytes.plugin.util

import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter
import javax.swing.filechooser.FileNameExtensionFilter

object UiUtils {

    private fun showDialog(title: String, filter: FileFilter,
                           defaultExtension: String, isSaveDialog: Boolean): String? {

        val fileChooser = JFileChooser()
        fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
        fileChooser.dialogTitle = title
        fileChooser.fileFilter = filter

        val approval: Int
        approval = if (isSaveDialog) {
            fileChooser.showSaveDialog(null)
        } else {
            fileChooser.showOpenDialog(null)
        }

        return if (approval == JFileChooser.APPROVE_OPTION) {
            var path = fileChooser.selectedFile.absolutePath
            if (!path.endsWith(defaultExtension)) {
                path += defaultExtension
            }
            path
        } else { null }
    }

    fun showJsonOpenDialog(title: String = "Open JSON file"): String? {
        return showDialog(title, FileNameExtensionFilter("JSON files", "json"),
                ".json", false)
    }

}