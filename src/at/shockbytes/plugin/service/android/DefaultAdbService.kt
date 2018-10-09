package at.shockbytes.plugin.service.android

import at.shockbytes.plugin.service.process.ProcessExecutionService
import at.shockbytes.plugin.util.IOUtils
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Author:  Martin Macheiner
 * Date:    16.03.2018
 */
class DefaultAdbService(private val processExecutionService: ProcessExecutionService) : AdbService {

    private var recordProcess: Process? = null

    override fun connectToDevice(deviceIp: String, port: Int): Single<Pair<String, String>> {
        return processExecutionService
                .executeCommands(listOf(
                        "adb tcpip $port",
                        "adb connect $deviceIp:$port",
                        "adb devices -l"))
                .map { Pair(it[1], it[2]) } // Grab data from connect and devices command
                .subscribeOn(Schedulers.io())
    }

    override fun connectToWearable(port: Int): Single<String> {
        return processExecutionService
                .executeCommandsGrabLastOutput(listOf(
                        "adb forward tcp:$port localabstract:/adb-hub",
                        "adb connect 127.0.0.1:$port"))
                .subscribeOn(Schedulers.io())
    }

    override fun disconnect(): Single<String> {
        return processExecutionService.executeCommand("adb usb")
                .map { "Switch back to USB connection.${System.lineSeparator()}$it${System.lineSeparator()}${System.lineSeparator()}" }
                .subscribeOn(Schedulers.io())
    }

    override fun discoverDeviceIp(): Single<Pair<String, String?>> {
        return processExecutionService
                .executeCommand("adb shell ip -f inet addr show wlan0")
                .map { output ->

                    val ipPrefix = "inet"
                    val lines = output.split(System.lineSeparator())
                    val line: String? = lines.find { it.startsWith(ipPrefix) }

                    if (line != null) {
                        val idxStart = line.indexOf(" ") + 1
                        val idxEnd = line.indexOf("/")
                        val deviceIp = line.substring(idxStart, idxEnd)
                        Pair("Device ip found <$deviceIp>", deviceIp)
                    } else {
                        Pair("No device available...", null)
                    }
                }
                .subscribeOn(Schedulers.io())
    }

    override fun restartAdbServer(): Single<String> {
        return processExecutionService
                .executeCommandsGrabLastOutput(listOf("adb kill-server", "adb start-server"))
                .map { "ADB server killed...${System.lineSeparator()}$it${System.lineSeparator()}${System.lineSeparator()}" }
                .subscribeOn(Schedulers.io())
    }

    override fun startScreenCapturing(): Single<String> {
        return processExecutionService
                .executeCommandAndReturnProcess("adb shell screenrecord $SCREEN_CAPTURE_TMP_FILE")
                .doOnEvent { (_, process), _ -> recordProcess = process }
                .map { "Start screen capturing...${System.lineSeparator()}${it.first}${System.lineSeparator()}" }
                .subscribeOn(Schedulers.io())
    }

    override fun stopScreenCapturing(filePath: String): Single<Pair<String, String>> {

        recordProcess?.destroy()
        return processExecutionService
                .executeCommand("adb pull $SCREEN_CAPTURE_TMP_FILE $filePath")
                .map {
                    val output = "$it${System.lineSeparator()}File copied to location: $filePath${System.lineSeparator()}"
                    Pair(output, filePath)
                }
                .subscribeOn(Schedulers.io())
    }

    companion object {

        private const val SCREEN_CAPTURE_TMP_FILE = "/storage/emulated/0/tmp_sb.mp4"
    }

}