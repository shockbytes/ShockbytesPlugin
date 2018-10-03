package at.shockbytes.plugin.util

import org.apache.commons.io.IOUtils
import java.io.*

object IOUtils {


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
    fun readProcessOutput(p: Process?): String {
        return BufferedReader(InputStreamReader(p?.inputStream)).lineSequence().joinToString("\n")
    }
}