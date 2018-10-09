package at.shockbytes.plugin.util

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

fun ByteArray.toHex() = this.joinToString(separator = "") { it.toInt().and(0xff).toString(16).padStart(2, '0') }

fun String.hexStringToByteArray() = ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

fun Disposable.addTo(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}

fun Process.readProcess(): String? {
    return try {
        return BufferedReader(InputStreamReader(this.inputStream)).lineSequence().joinToString(System.lineSeparator())
    } catch (e: IOException) {
        null
    }
}