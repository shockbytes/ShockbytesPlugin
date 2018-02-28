package at.shockbytes.plugin.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.BiPredicate
import java.util.stream.Collectors

/**
 * Author:  Martin Macheiner
 * Date:    03.08.2016.
 */
object HelperUtil {

    private val SDF_DATE = SimpleDateFormat("dd.MM.yy")
    private val SDF_TIME = SimpleDateFormat("kk:mm")

    fun formatDate(date: Long): String {
        return SDF_DATE.format(Date(date))
    }

    fun formatTime(time: Long): String {
        return SDF_TIME.format(Date(time))
    }

    fun getMinutesBetweeen(start: Long, end: Long): Int {
        val diff = end - start
        val seconds = diff / 1000L
        return (seconds / 60).toInt()
    }

    fun toHexString(block: ByteArray): String {
        val hex = block.toHex()
        val formatted = StringBuilder()
        for (i in 0 until hex.length step 2) {
            formatted.append(hex.substring(i, i + 2).toUpperCase())
            if (i < hex.length - 2) {
                formatted.append(":")
            }
        }
        return formatted.toString()
    }

    @Throws(IOException::class)
    fun copyFile(inPath: String, outPath: String) {

        val outFile = File(outPath)
        outFile.createNewFile()

        val inStream = FileInputStream(inPath)
        val out = FileOutputStream(outFile)

        IOUtils.copy(inStream, out)

        inStream.close()
        out.close()
    }

    fun deleteFile(f: File) {
        if (f.exists()) {
            f.delete()
        }
    }

    @Throws(IOException::class)
    fun getOutputFromProcess(p: Process?): String {

        if (p == null) {
            return ""
        }
        //InputStream stream = useErrorStream ? p.getErrorStream() : p.getInputStream();
        val stream = p.inputStream
        val inStream = BufferedReader(InputStreamReader(stream))
        val sb = StringBuilder()
        inStream.useLines {
            it.forEach {
                sb.append(it)
                sb.append("\n")
            }
        }
        return sb.toString()
    }

    fun getPackagesFromProject(project: Project): List<String> {

        val vFiles = ProjectRootManager.getInstance(project).contentSourceRoots
        vFiles.forEach { f ->
            try {
                return Files.find(Paths.get(f.path), 999, BiPredicate { _, bfa -> bfa.isDirectory })
                        .map { p -> p.toFile().absolutePath.replace("\\", "/").replace(f.path, "") }
                        .filter { s -> !s.isEmpty() && StringUtils.countMatches(s, "/") > 3 }
                        .collect(Collectors.toList())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return listOf()
    }

    fun getDisplayablePackageName(packages: List<String>): List<String> {
        return packages.map { s -> s.substring(StringUtils.ordinalIndexOf(s, "/", 4)) }
    }

    fun getSourceRootFolder(project: Project): String? {
        val vFiles = ProjectRootManager.getInstance(project).contentSourceRoots
        return if (vFiles.isNotEmpty()) {
            vFiles[0].path
        } else null
    }

    fun getProjectRootFolder(project: Project): String? {
        return project.basePath
    }

    // Extension functions
    fun ByteArray.toHex() = this.joinToString(separator = "") { it.toInt().and(0xff).toString(16).padStart(2, '0') }

    fun String.hexStringToByteArray() = ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

}
