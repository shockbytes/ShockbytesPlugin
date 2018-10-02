package at.shockbytes.plugin.util

import java.text.SimpleDateFormat
import java.util.*

object PluginUtils {

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
}