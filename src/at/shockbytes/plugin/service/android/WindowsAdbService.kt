package at.shockbytes.plugin.service.android

import at.shockbytes.plugin.util.HelperUtil
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Author:  Martin Macheiner
 * Date:    16.03.2018
 */
class WindowsAdbService : AdbService {

    override fun connectToDevice(deviceIp: String, port: Int): Single<Pair<String, String>> {
        return Single.fromCallable {
            val cmdTcpIp = "adb tcpip $port"
            val cmdConnect = "adb connect $deviceIp:$port"
            val cmdDevices = "adb devices -l"

            Runtime.getRuntime().exec(cmdTcpIp)
            val connectProcess = Runtime.getRuntime().exec(cmdConnect)
            val connectionProcessOutput = "${HelperUtil.getOutputFromProcess(connectProcess)}\n"
            val deviceProcess = Runtime.getRuntime().exec(cmdDevices)
            val connectedDevice = HelperUtil.getOutputFromProcess(deviceProcess)

            Pair(connectionProcessOutput, connectedDevice)
        }.subscribeOn(Schedulers.io())
    }

    override fun connectToWearable(port: Int): Single<String> {
        return Single.fromCallable {
            try {
                val cmdForwardTcp = "adb forward tcp:$port localabstract:/adb-hub"
                val cmdConnect = "adb connect 127.0.0.1:$port"

                Runtime.getRuntime().exec(cmdForwardTcp)
                val p = Runtime.getRuntime().exec(cmdConnect)
                "${HelperUtil.getOutputFromProcess(p)}\n\n"
            } catch (e: IOException) {
                "Unable to connect to wearable: ${e.localizedMessage}\n"
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun disconnect(): Single<String> {
        return Single.fromCallable {
            try {
                val cmdDisconnect = "adb usb"
                val disconnectProcess = Runtime.getRuntime().exec(cmdDisconnect)
                "Switch back to USB connection.\n${HelperUtil.getOutputFromProcess(disconnectProcess)}\n\n"
            } catch (e: IOException) {
                "Unable to disconnect due to following reason: ${e.localizedMessage}"
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun discoverDeviceIp(): Single<Pair<String, String?>> {
        return Single.fromCallable {
            val cmdShowIp = "adb shell ip -f inet addr show wlan0"
            val p = Runtime.getRuntime().exec(cmdShowIp)
            val inStream = BufferedReader(InputStreamReader(p.inputStream))
            val ipPrefix = "inet"

            try {
                inStream.useLines { lines ->
                    lines.map { it.trim { it <= ' ' } }
                            .filter { it.startsWith(ipPrefix) }
                            .forEach { line ->
                                val idxStart = line.indexOf(" ") + 1
                                val idxEnd = line.indexOf("/")

                                val deviceIp = line.substring(idxStart, idxEnd)
                                return@fromCallable Pair("Device ip found <$deviceIp>", deviceIp)
                            }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Pair("Cannot discover device due to: ${e.localizedMessage}", null)
            }
            Pair("No device available...", null)
        }.subscribeOn(Schedulers.io())
    }

    override fun restartAdbServer(): Single<String> {
        return Single.fromCallable {
            val cmdKillServer = "adb kill-server"
            val cmdStartServer = "adb start-server"
            try {

                Runtime.getRuntime().exec(cmdKillServer)
                val p = Runtime.getRuntime().exec(cmdStartServer)
                val output = HelperUtil.getOutputFromProcess(p)

                "ADB server killed...\n$output\n\n"
            } catch (e: IOException) {
                e.printStackTrace()
                "ADB server couldn't be killed: ${e.localizedMessage}"
            }
        }.subscribeOn(Schedulers.io())
    }

}